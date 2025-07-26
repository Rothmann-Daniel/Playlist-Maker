package com.example.playlistmaker.search.data.network


import com.example.playlistmaker.search.data.dto.TrackDto

interface NetworkClient {
    suspend fun searchTracks(query: String): List<TrackDto>
}