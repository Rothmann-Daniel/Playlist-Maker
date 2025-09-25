package com.example.playlistmaker.search.domain.repository

import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    suspend fun addTrack(track: Track)
    fun getHistory(): Flow<List<Track>>
    suspend fun clearHistory()
}