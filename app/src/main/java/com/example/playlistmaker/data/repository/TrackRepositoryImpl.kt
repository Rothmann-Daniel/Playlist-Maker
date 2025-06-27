package com.example.playlistmaker.data.repository

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository
import com.example.playlistmaker.data.network.NetworkClient
import com.example.playlistmaker.data.dto.toTrack
import com.example.playlistmaker.domain.repository.SearchHistoryRepository


class TrackRepositoryImpl(
    private val networkClient: NetworkClient,
    private val searchHistoryRepository: SearchHistoryRepository // Используем интерфейс репозитория
) : TrackRepository {

    override suspend fun searchTracks(query: String): List<Track> {
        return networkClient.searchTracks(query).map { it.toTrack() }
    }

    override suspend fun addTrackToHistory(track: Track) {
        searchHistoryRepository.addTrack(track) // Заменяем на вызов метода репозитория
    }

    override suspend fun getSearchHistory(): List<Track> {
        return searchHistoryRepository.getHistory() // Заменяем на вызов метода репозитория
    }

    override suspend fun clearSearchHistory() {
        searchHistoryRepository.clearHistory() // Заменяем на вызов метода репозитория
    }
}