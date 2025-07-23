package com.example.playlistmaker.search.domain.usecase

import com.example.playlistmaker.search.domain.repository.TrackRepository

class ClearSearchHistoryUseCase(
    private val repository: TrackRepository
) {
    suspend operator fun invoke() {
        repository.clearSearchHistory()
    }
}