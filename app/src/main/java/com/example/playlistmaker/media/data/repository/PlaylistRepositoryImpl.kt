package com.example.playlistmaker.media.data.repository

import android.net.Uri
import com.example.playlistmaker.media.data.db.dao.PlaylistDao
import com.example.playlistmaker.media.data.db.dao.PlaylistTrackDataDao
import com.example.playlistmaker.media.data.db.dao.TrackDao
import com.example.playlistmaker.media.data.db.converters.PlaylistDbConverter
import com.example.playlistmaker.media.data.db.converters.PlaylistTrackDataConverter
import com.example.playlistmaker.media.data.storage.PlaylistFileStorage
import com.example.playlistmaker.media.domain.model.Playlist
import com.example.playlistmaker.media.domain.repository.PlaylistRepository
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val playlistTrackDataDao: PlaylistTrackDataDao,
    private val favoriteTrackDao: TrackDao,
    private val fileStorage: PlaylistFileStorage
) : PlaylistRepository {

    override suspend fun createPlaylist(
        name: String,
        description: String?,
        coverImageUri: Uri?
    ): Long {
        val coverPath = coverImageUri?.let {
            fileStorage.saveCoverImageFromUri(it)
        }

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
        val playlist = getPlaylistById(playlistId)

        playlist?.coverImagePath?.let { coverPath ->
            fileStorage.deleteCoverImage(coverPath)
        }

        // Удаление плейлиста автоматически удалит все треки благодаря CASCADE
        playlistDao.deletePlaylist(playlistId)
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists()
            .map { entities ->
                entities.map { entity ->
                    val trackIds = playlistTrackDataDao.getTrackIdsByPlaylistId(entity.playlistId)
                    PlaylistDbConverter.mapEntityToPlaylist(entity, trackIds)
                }
            }
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        val entity = playlistDao.getPlaylistById(playlistId) ?: return null
        val trackIds = playlistTrackDataDao.getTrackIdsByPlaylistId(playlistId)
        return PlaylistDbConverter.mapEntityToPlaylist(entity, trackIds)
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, track: Track) {
        val trackEntity = PlaylistTrackDataConverter.mapTrackToEntity(track, playlistId)
        playlistTrackDataDao.insertTrack(trackEntity)

        // Обновляем счетчик треков
        updatePlaylistTrackCount(playlistId)
    }

   override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Int) {
        // Удаляем трек из плейлиста
        playlistTrackDataDao.removeTrackFromPlaylist(playlistId, trackId)

        // Проверяем, используется ли трек в других плейлистах
        val playlistsCount = playlistTrackDataDao.countPlaylistsWithTrack(trackId)

        // Если трек больше не используется ни в одном плейлисте - удаляем его из таблицы
        if (playlistsCount == 0) {
            // Трек можно удалить из playlist_track_data через каскадное удаление
            // или он уже удален выше
        }

        // Обновляем счетчик треков в плейлисте
        updatePlaylistTrackCount(playlistId)
    }

    override suspend fun isTrackInPlaylist(playlistId: Long, trackId: Int): Boolean {
        return playlistTrackDataDao.isTrackInPlaylist(playlistId, trackId)
    }

    override suspend fun getTracksForPlaylist(playlistId: Long): List<Track> {
        val trackEntities = playlistTrackDataDao.getTracksByPlaylistId(playlistId)

        if (trackEntities.isEmpty()) return emptyList()

        val favoriteTrackIds = favoriteTrackDao.getFavoriteTrackIds().first()

        return trackEntities.map { entity ->
            PlaylistTrackDataConverter.mapEntityToTrack(
                entity,
                isFavorite = entity.trackId in favoriteTrackIds
            )
        }
    }

    override suspend fun updatePlaylistTrackCount(playlistId: Long, newCount: Int) {
        val playlist = playlistDao.getPlaylistById(playlistId) ?: return
        val updatedPlaylist = playlist.copy(tracksCount = newCount)
        playlistDao.updatePlaylist(updatedPlaylist)
    }

    override suspend fun updatePlaylistTrackIds(playlistId: Long, trackIds: List<Int>) {
        // Этот метод больше не нужен, т.к. trackIds хранятся в связанной таблице
        // Но для совместимости оставим обновление счетчика
        updatePlaylistTrackCount(playlistId, trackIds.size)
    }

    private suspend fun updatePlaylistTrackCount(playlistId: Long) {
        val count = playlistTrackDataDao.getTracksCount(playlistId)
        updatePlaylistTrackCount(playlistId, count)
    }

    override suspend fun getTracksByIds(trackIds: List<Int>): List<Track> {
        if (trackIds.isEmpty()) return emptyList()

        val allTracks = playlistTrackDataDao.getAllTracks()
        val favoriteTrackIds = favoriteTrackDao.getFavoriteTrackIds().first()

        return allTracks
            .filter { it.trackId in trackIds }
            .map { entity ->
                PlaylistTrackDataConverter.mapEntityToTrack(
                    entity,
                    isFavorite = entity.trackId in favoriteTrackIds
                )
            }
    }
}
