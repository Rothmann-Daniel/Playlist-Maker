package com.example.playlistmaker.media.data.repository

import android.net.Uri
import com.example.playlistmaker.media.data.db.AppDatabase
import com.example.playlistmaker.media.data.db.converters.PlaylistDbConverter
import com.example.playlistmaker.media.data.db.converters.PlaylistTrackDataConverter
import com.example.playlistmaker.media.data.db.entity.PlaylistTrackEntity
import com.example.playlistmaker.media.data.storage.PlaylistFileStorage
import com.example.playlistmaker.media.domain.model.Playlist
import com.example.playlistmaker.media.domain.repository.PlaylistRepository
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val database: AppDatabase,
    private val fileStorage: PlaylistFileStorage
) : PlaylistRepository {

    private val playlistDao = database.playlistDao()
    private val playlistTrackDao = database.playlistTrackDao()
    private val playlistTrackDataDao = database.playlistTrackDataDao()
    private val favoriteTrackDao = database.trackDao()

    override suspend fun createPlaylist(
        name: String,
        description: String?,
        coverImageUri: Uri?
    ): Long {
        // Сохраняем изображение обложки
        val coverPath = coverImageUri?.let {
            fileStorage.saveCoverImageFromUri(it)
        }

        // Создаем плейлист
        val playlist = Playlist(
            name = name,
            description = description,
            coverImagePath = coverPath,
            trackIds = emptyList(),
            tracksCount = 0
        )

        val entity = PlaylistDbConverter.mapPlaylistToEntity(playlist)
        return playlistDao.insertPlaylist(entity)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        val entity = PlaylistDbConverter.mapPlaylistToEntity(playlist)
        playlistDao.updatePlaylist(entity)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        // Получаем плейлист для удаления обложки
        val playlist = getPlaylistById(playlistId)

        // Удаляем файл обложки
        playlist?.coverImagePath?.let { coverPath ->
            fileStorage.deleteCoverImage(coverPath)
        }

        // Получаем ID треков плейлиста перед удалением связей
        val trackIds = playlist?.trackIds ?: emptyList()

        // Удаляем плейлист из БД
        playlistDao.deletePlaylist(playlistId)

        // Удаляем неиспользуемые треки
        trackIds.forEach { trackId ->
            cleanupUnusedTrack(trackId)
        }
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists()
            .map { entities ->
                entities.map { PlaylistDbConverter.mapEntityToPlaylist(it) }
            }
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return playlistDao.getPlaylistById(playlistId)?.let {
            PlaylistDbConverter.mapEntityToPlaylist(it)
        }
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, track: Track) {
        // Сохраняем данные трека
        val trackDataEntity = PlaylistTrackDataConverter.mapTrackToEntity(track)
        playlistTrackDataDao.insertTrack(trackDataEntity)

        // Создаем связь трек-плейлист
        val playlistTrackEntity = PlaylistTrackEntity(playlistId, track.trackId)
        playlistTrackDao.insertPlaylistTrack(playlistTrackEntity)
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Int) {
        // Удаляем связь
        playlistTrackDao.removeTrackFromPlaylist(playlistId, trackId)

        // Очищаем неиспользуемый трек
        cleanupUnusedTrack(trackId)
    }

    override suspend fun isTrackInPlaylist(playlistId: Long, trackId: Int): Boolean {
        return playlistTrackDao.isTrackInPlaylist(playlistId, trackId) > 0
    }

    override suspend fun getTracksForPlaylist(playlistId: Long): List<Track> {
        // Получаем ID треков плейлиста
        val playlistTrackEntities = playlistTrackDao.getTracksForPlaylist(playlistId)
        val trackIds = playlistTrackEntities.map { it.trackId }

        if (trackIds.isEmpty()) return emptyList()

        // Получаем данные треков
        val trackDataEntities = playlistTrackDataDao.getTracksByIds(trackIds)

        // Получаем ID избранных треков
        val favoriteTrackIds = favoriteTrackDao.getFavoriteTrackIds().first()

        // Преобразуем в Track с правильным флагом isFavorite
        return trackDataEntities.map { entity ->
            PlaylistTrackDataConverter.mapEntityToTrack(
                entity,
                isFavorite = entity.trackId in favoriteTrackIds
            )
        }
    }

    override suspend fun updatePlaylistTrackCount(playlistId: Long, newCount: Int) {
        val playlist = getPlaylistById(playlistId) ?: return
        val updatedPlaylist = playlist.copy(tracksCount = newCount)
        updatePlaylist(updatedPlaylist)
    }

    override suspend fun updatePlaylistTrackIds(playlistId: Long, trackIds: List<Int>) {
        val playlist = getPlaylistById(playlistId) ?: return
        val updatedPlaylist = playlist.copy(
            trackIds = trackIds,
            tracksCount = trackIds.size
        )
        updatePlaylist(updatedPlaylist)
    }

    /**
     * Удаляет трек из таблицы playlist_track_data, если он не используется ни в одном плейлисте
     */
    private suspend fun cleanupUnusedTrack(trackId: Int) {
        playlistTrackDataDao.deleteUnusedTrack(trackId)
    }
}