package com.example.playlistmaker.search.data.repository

import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.domain.repository.TrackRepository
import com.example.playlistmaker.search.data.network.NetworkClient
import com.example.playlistmaker.search.data.dto.toTrack
import com.example.playlistmaker.search.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class TrackRepositoryImpl(
    private val networkClient: NetworkClient,
    private val searchHistoryRepository: SearchHistoryRepository
) : TrackRepository {

    override fun searchTracks(query: String): Flow<List<Track>> = flow {
        val tracks = networkClient.searchTracks(query).map { it.toTrack() }
        emit(tracks)
    }

    override suspend fun addTrackToHistory(track: Track) {
        searchHistoryRepository.addTrack(track)
    }

    override fun getSearchHistory(): Flow<List<Track>> {
        return searchHistoryRepository.getHistory()
    }

    override suspend fun clearSearchHistory() {
        searchHistoryRepository.clearHistory()
    }
}