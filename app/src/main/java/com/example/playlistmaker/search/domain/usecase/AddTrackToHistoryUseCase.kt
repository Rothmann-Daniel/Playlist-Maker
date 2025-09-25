package com.example.playlistmaker.search.domain.usecase

import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.domain.repository.TrackRepository

class AddTrackToHistoryUseCase(
    private val repository: TrackRepository
) {
    suspend operator fun invoke(track: Track) {
        repository.addTrackToHistory(track)
    }
}