package com.example.playlistmaker.search.data.repository

import com.example.playlistmaker.media.data.db.AppDatabase
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.domain.repository.TrackRepository
import com.example.playlistmaker.search.data.network.NetworkClient
import com.example.playlistmaker.search.data.dto.toTrack
import com.example.playlistmaker.search.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TrackRepositoryImpl(
    private val networkClient: NetworkClient,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val database: AppDatabase
) : TrackRepository {

    private val trackDao = database.trackDao()

    override fun searchTracks(query: String): Flow<List<Track>> {
        return networkClient.searchTracksFlow(query)
            .combine(trackDao.getFavoriteTrackIds()) { tracksDto, favoriteIds ->
                tracksDto.map { dto ->
                    val track = dto.toTrack()
                    track.copy(isFavorite = favoriteIds.contains(track.trackId))
                }
            }
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