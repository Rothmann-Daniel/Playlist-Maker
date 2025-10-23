package com.example.playlistmaker.media.domain.interactor

import android.net.Uri
import com.example.playlistmaker.media.domain.model.Playlist
import com.example.playlistmaker.media.domain.repository.PlaylistRepository
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

/**
 * Интерактор для работы с плейлистами Содержит всю бизнес-логику
 */
class PlaylistInteractor(
    private val repository: PlaylistRepository
) {

    /**
     * Создает новый плейлист с валидацией
     */
    suspend fun createPlaylist(
        name: String,
        description: String?,
        coverImageUri: Uri?
    ): Result<Long> {
        return try {
            // Валидация названия
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Playlist name cannot be empty"))
            }

            val playlistId = repository.createPlaylist(
                name = name.trim(),
                description = description?.trim()?.takeIf { it.isNotEmpty() },
                coverImageUri = coverImageUri
            )

            Result.success(playlistId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Обновляет плейлист
     */
    suspend fun updatePlaylist(playlist: Playlist): Result<Unit> {
        return try {
            if (playlist.name.isBlank()) {
                return Result.failure(IllegalArgumentException("Playlist name cannot be empty"))
            }

            repository.updatePlaylist(playlist)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Удаляет плейлист
     */
    suspend fun deletePlaylist(playlistId: Long): Result<Unit> {
        return try {
            repository.deletePlaylist(playlistId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Получает все плейлисты
     */
    fun getAllPlaylists(): Flow<List<Playlist>> {
        return repository.getAllPlaylists()
    }

    /**
     * Получает плейлист по ID
     */
    suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return repository.getPlaylistById(playlistId)
    }

    /**
     * Добавляет трек в плейлист с обновлением счетчиков
     */
    suspend fun addTrackToPlaylist(playlistId: Long, track: Track): Result<Unit> {
        return try {
            // Проверяем, есть ли уже трек в плейлисте
            val isInPlaylist = repository.isTrackInPlaylist(playlistId, track.trackId)
            if (isInPlaylist) {
                return Result.failure(TrackAlreadyInPlaylistException())
            }

            // Получаем текущий плейлист
            val playlist = repository.getPlaylistById(playlistId)
                ?: return Result.failure(PlaylistNotFoundException())

            // Добавляем трек
            repository.addTrackToPlaylist(playlistId, track)

            // Обновляем список ID треков и счетчик
            val updatedTrackIds = playlist.trackIds + track.trackId
            repository.updatePlaylistTrackIds(playlistId, updatedTrackIds)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Удаляет трек из плейлиста с обновлением счетчиков
     */
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Int): Result<Unit> {
        return try {
            // Получаем текущий плейлист
            val playlist = repository.getPlaylistById(playlistId)
                ?: return Result.failure(PlaylistNotFoundException())

            // Удаляем трек
            repository.removeTrackFromPlaylist(playlistId, trackId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Проверяет наличие трека в плейлисте
     */
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Int): Boolean {
        return repository.isTrackInPlaylist(playlistId, trackId)
    }

    /**
     * Получает все треки плейлиста
     */
    suspend fun getTracksForPlaylist(playlistId: Long): List<Track> {
        return repository.getTracksForPlaylist(playlistId)
    }

    /**
     * Получает информацию о плейлисте с количеством треков
     */
    suspend fun getPlaylistInfo(playlistId: Long): PlaylistInfo? {
        val playlist = repository.getPlaylistById(playlistId) ?: return null
        val tracks = repository.getTracksForPlaylist(playlistId)

        return PlaylistInfo(
            playlist = playlist,
            tracks = tracks,
            totalDuration = tracks.sumOf { it.trackTimeMillis }
        )
    }
}

/**
 * Информация о плейлисте с треками
 */
data class PlaylistInfo(
    val playlist: Playlist,
    val tracks: List<Track>,
    val totalDuration: Long
)

/**
 * Исключения для работы с плейлистами
 */
class TrackAlreadyInPlaylistException : Exception("Track already exists in playlist")
class PlaylistNotFoundException : Exception("Playlist not found")