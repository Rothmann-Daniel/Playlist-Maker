package com.example.playlistmaker.data.repository

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository
import com.example.playlistmaker.data.network.NetworkClient

import com.example.playlistmaker.data.dto.toTrack
import com.example.playlistmaker.ui.search.SearchHistory

class TrackRepositoryImpl(
    private val networkClient: NetworkClient,
    private val searchHistory: SearchHistory // Добавляем зависимость
) : TrackRepository {

    override suspend fun searchTracks(query: String): List<Track> {
        return networkClient.searchTracks(query).map { it.toTrack() }
    }

    override suspend fun addTrackToHistory(track: Track) {
        searchHistory.addTrack(track) // Делегируем SearchHistory
    }

    override suspend fun getSearchHistory(): List<Track> {
        return searchHistory.getHistory()
    }

    override suspend fun clearSearchHistory() {
        searchHistory.clearHistory()
    }

}