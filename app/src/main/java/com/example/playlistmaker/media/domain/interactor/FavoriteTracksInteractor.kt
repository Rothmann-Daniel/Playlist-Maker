package com.example.playlistmaker.media.domain.interactor

import com.example.playlistmaker.media.domain.repository.FavoriteTracksRepository
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

class FavoriteTracksInteractor(
    private val repository: FavoriteTracksRepository
) {
    suspend fun addTrackToFavorites(track: Track) {
        repository.addTrackToFavorites(track)
    }

    suspend fun removeTrackFromFavorites(track: Track) {
        repository.removeTrackFromFavorites(track)
    }

    fun getAllFavoriteTracks(): Flow<List<Track>> {
        return repository.getAllFavoriteTracks()
    }
}