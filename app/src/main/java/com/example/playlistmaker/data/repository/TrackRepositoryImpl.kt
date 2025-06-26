package com.example.playlistmaker.data.repository

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository
import com.example.playlistmaker.data.network.NetworkClient
import com.example.playlistmaker.data.dto.TrackDto
import com.example.playlistmaker.data.dto.toTrack
import com.example.playlistmaker.ui.search.SearchHistory

class TrackRepositoryImpl(
    private val networkClient: NetworkClient
    private val searchHistory: SearchHistory // Добавляем зависимость
) : TrackRepository {

    override suspend fun searchTracks(query: String): Result<List<Track>> {
        return try {
            // Запрос к сети через NetworkClient
            val result = networkClient.searchTracks(query)

            // Преобразуем DTO в domain-модель
            Result.success(result.map { it.toTrack() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addTrackToHistory(track: Track) {
        // Реализация сохранения трека в историю
        // Например: database.historyDao().insert(track.toEntity())
    }

    override suspend fun getSearchHistory(): List<Track> {
        // Реализация получения истории поиска
        // Например: return database.historyDao().getAll().map { it.toDomain() }
        return emptyList() // Заглушка - замените на реальную реализацию
    }

    override suspend fun clearSearchHistory() {
        // Реализация очистки истории
        // Например: database.historyDao().clearAll()
    }

}