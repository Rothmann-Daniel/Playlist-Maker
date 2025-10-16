package com.example.playlistmaker.media.data.repository

import com.example.playlistmaker.media.data.db.AppDatabase
import com.example.playlistmaker.media.data.db.converters.PlaylistDbConverter
import com.example.playlistmaker.media.data.db.converters.PlaylistTrackDataConverter
import com.example.playlistmaker.media.data.db.entity.PlaylistTrackEntity
import com.example.playlistmaker.media.domain.model.Playlist
import com.example.playlistmaker.media.domain.repository.PlaylistRepository
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val database: AppDatabase
) : PlaylistRepository {

    private val playlistDao = database.playlistDao()
    private val playlistTrackDao = database.playlistTrackDao()
    private val playlistTrackDataDao = database.playlistTrackDataDao()
    private val favoriteTrackDao = database.trackDao()

    override suspend fun createPlaylist(playlist: Playlist): Long {
        val entity = PlaylistDbConverter.mapPlaylistToEntity(playlist)
        return playlistDao.insertPlaylist(entity)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        val entity = PlaylistDbConverter.mapPlaylistToEntity(playlist)
        playlistDao.updatePlaylist(entity)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
        // Удаляем связи этого плейлиста с треками
        // Room может делать каскадное удаление, если настроено
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
        // Сохраняем данные трека в отдельную таблицу плейлистов
        val trackDataEntity = PlaylistTrackDataConverter.mapTrackToEntity(track)
        playlistTrackDataDao.insertTrack(trackDataEntity)

        // Добавляем связь трек-плейлист
        val playlistTrackEntity = PlaylistTrackEntity(playlistId, track.trackId)
        playlistTrackDao.insertPlaylistTrack(playlistTrackEntity)

        // Обновляем счетчик треков в плейлисте
        val playlist = getPlaylistById(playlistId)
        playlist?.let {
            val trackIds = it.trackIds + track.trackId
            val updatedPlaylist = it.copy(
                trackIds = trackIds,
                tracksCount = trackIds.size
            )
            updatePlaylist(updatedPlaylist)
        }
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
}