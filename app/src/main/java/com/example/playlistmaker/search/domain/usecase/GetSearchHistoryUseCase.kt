package com.example.playlistmaker.search.domain.usecase

import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow

class GetSearchHistoryUseCase(
    private val repository: TrackRepository
) {
    operator fun invoke(): Flow<List<Track>> {
        return repository.getSearchHistory()
    }
}