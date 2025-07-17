package com.example.playlistmaker.search.domain.usecase

import com.example.playlistmaker.search.domain.model.NetworkResult
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.domain.repository.TrackRepository

class SearchTracksUseCase(
    private val repository: TrackRepository // Зависимость - репозиторий абстракция для доступа к данным (сеть/БД)
) {
    suspend operator fun invoke(query: String): NetworkResult<List<Track>> { //метод может быть приостановлен (корутины)
        // Реализация
        return try {
            val tracks = repository.searchTracks(query)
            NetworkResult.Success(tracks) //sealed-класс для обработки успеха/ошибки
        } catch (e: Exception) {
            NetworkResult.Failure(e.message ?: "Unknown error")
        }
    }
}