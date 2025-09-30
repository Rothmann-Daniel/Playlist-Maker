package com.example.playlistmaker.search.data.network


import com.example.playlistmaker.search.data.dto.TrackDto
import kotlinx.coroutines.flow.Flow

interface NetworkClient {
    suspend fun searchTracks(query: String): List<TrackDto>
    fun searchTracksFlow(query: String): Flow<List<TrackDto>>
}