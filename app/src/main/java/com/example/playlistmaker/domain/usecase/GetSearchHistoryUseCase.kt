package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.repository.TrackRepository
import com.example.playlistmaker.domain.model.Track

class GetSearchHistoryUseCase(
    private val repository: TrackRepository
) {
    suspend operator fun invoke(): List<Track> {
        return repository.getSearchHistory()
    }
}