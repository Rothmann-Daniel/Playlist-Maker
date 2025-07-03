package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.repository.TrackRepository
import com.example.playlistmaker.domain.model.Track

class AddTrackToHistoryUseCase(
    private val repository: TrackRepository
) {
    suspend operator fun invoke(track: Track) {
        repository.addTrackToHistory(track)
    }
}