package com.example.playlistmaker.media.data.repository

import com.example.playlistmaker.media.data.db.AppDatabase
import com.example.playlistmaker.media.data.db.converters.PlaylistDbConverter
import com.example.playlistmaker.media.data.db.converters.TrackDbConverter
import com.example.playlistmaker.media.data.db.entity.PlaylistTrackEntity
import com.example.playlistmaker.media.domain.model.Playlist
import com.example.playlistmaker.media.domain.repository.PlaylistRepository
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val database: AppDatabase
) : PlaylistRepository {

    private val playlistDao = database.playlistDao()
    private val playlistTrackDao = database.playlistTrackDao()
    private val trackDao = database.trackDao()

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
        // Также удаляем все связи этого плейлиста с треками
        // (можно добавить каскадное удаление)
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
        // Сохраняем трек в таблицу треков (если еще не сохранен)
        val trackEntity = TrackDbConverter.mapTrackToEntity(track)
        trackDao.insertTrack(trackEntity)

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
}