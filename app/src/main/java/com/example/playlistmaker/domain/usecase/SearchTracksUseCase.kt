package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.model.NetworkResult
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository

class SearchTracksUseCase(
    private val repository: TrackRepository
) {
    suspend operator fun invoke(query: String): NetworkResult<List<Track>> {
        return try {
            val tracks = repository.searchTracks(query)
            NetworkResult.Success(tracks)
        } catch (e: Exception) {
            NetworkResult.Failure(e.message ?: "Unknown error")
        }
    }
}