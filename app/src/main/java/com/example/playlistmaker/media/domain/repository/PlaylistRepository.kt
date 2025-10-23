package com.example.playlistmaker.media.domain.repository

import android.net.Uri
import com.example.playlistmaker.media.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow


interface PlaylistRepository {

    /**
     * Создает новый плейлист
     * @return ID созданного плейлиста
     */
    suspend fun createPlaylist(
        name: String,
        description: String?,
        coverImageUri: Uri?
    ): Long

    /**
     * Обновляет существующий плейлист
     */
    suspend fun updatePlaylist(playlist: Playlist)

    /**
     * Удаляет плейлист и все его связи
     */
    suspend fun deletePlaylist(playlistId: Long)

    /**
     * Получает все плейлисты
     */
    fun getAllPlaylists(): Flow<List<Playlist>>

    /**
     * Получает плейлист по ID
     */
    suspend fun getPlaylistById(playlistId: Long): Playlist?

    /**
     * Добавляет трек в плейлист
     */
    suspend fun addTrackToPlaylist(playlistId: Long, track: Track)

    /**
     * Удаляет трек из плейлиста
     */
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Int)

    /**
     * Проверяет наличие трека в плейлисте
     */
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Int): Boolean

    /**
     * Получает все треки плейлиста
     */
    suspend fun getTracksForPlaylist(playlistId: Long): List<Track>

    /**
     * Обновляет количество треков в плейлисте
     */
    suspend fun updatePlaylistTrackCount(playlistId: Long, newCount: Int)

    /**
     * Обновляет список ID треков в плейлисте
     */
    suspend fun updatePlaylistTrackIds(playlistId: Long, trackIds: List<Int>)

    /**
     * список треков по их ID из локальной базы данных
     */
    suspend fun getTracksByIds(trackIds: List<Int>): List<Track>
}