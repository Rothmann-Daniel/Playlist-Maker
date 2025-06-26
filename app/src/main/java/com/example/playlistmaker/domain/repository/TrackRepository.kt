package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    suspend fun searchTracks(query: String): Result<List<Track>>  // Для поиска треков
    suspend fun addTrackToHistory(track: Track)         // Для добавления в историю
    suspend fun getSearchHistory(): List<Track>         // Для получения истории
    suspend fun clearSearchHistory()
}