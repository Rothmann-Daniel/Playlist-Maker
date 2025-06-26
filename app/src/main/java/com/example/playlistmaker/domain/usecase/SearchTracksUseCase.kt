package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.repository.TrackRepository
import com.example.playlistmaker.domain.model.Track


class SearchTracksUseCase(
    private val trackRepository: TrackRepository
) {
    suspend operator fun invoke(query: String): Result<List<Track>> {
        return trackRepository.searchTracks(query)
    }
}