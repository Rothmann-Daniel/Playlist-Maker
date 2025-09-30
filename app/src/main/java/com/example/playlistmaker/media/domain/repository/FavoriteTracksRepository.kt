package com.example.playlistmaker.media.domain.repository

import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksRepository {
    suspend fun addTrackToFavorites(track: Track)
    suspend fun removeTrackFromFavorites(track: Track)
    fun getAllFavoriteTracks(): Flow<List<Track>>
}