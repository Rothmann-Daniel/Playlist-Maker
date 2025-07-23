package com.example.playlistmaker.search.domain.usecase

import com.example.playlistmaker.search.domain.repository.TrackRepository
import com.example.playlistmaker.search.domain.model.Track

class AddTrackToHistoryUseCase(
    private val repository: TrackRepository
) {
    suspend operator fun invoke(track: Track) {
        repository.addTrackToHistory(track)
    }
}