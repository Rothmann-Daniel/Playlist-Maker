package com.example.playlistmaker.search.domain.usecase


import com.example.playlistmaker.search.domain.model.NetworkResult
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchTracksUseCase(
    private val repository: TrackRepository
) {
    operator fun invoke(query: String): Flow<NetworkResult<List<Track>>> = flow {
        emit(NetworkResult.Loading)
        try {
            repository.searchTracks(query).collect { tracks ->
                emit(NetworkResult.Success(tracks))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Failure(e.message ?: "Unknown error"))
        }
    }
}
