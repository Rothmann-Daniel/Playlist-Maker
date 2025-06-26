package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.repository.TrackRepository

class ClearSearchHistoryUseCase(
    private val repository: TrackRepository
) {
    suspend operator fun invoke() {
        repository.clearSearchHistory()
    }
}