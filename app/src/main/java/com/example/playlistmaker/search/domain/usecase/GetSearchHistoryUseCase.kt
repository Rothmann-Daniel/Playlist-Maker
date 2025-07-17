package com.example.playlistmaker.search.domain.usecase

import com.example.playlistmaker.search.domain.repository.TrackRepository
import com.example.playlistmaker.search.domain.model.Track

class GetSearchHistoryUseCase(
    private val repository: TrackRepository
) {
    suspend operator fun invoke(): List<Track> {
        return repository.getSearchHistory()
    }
}