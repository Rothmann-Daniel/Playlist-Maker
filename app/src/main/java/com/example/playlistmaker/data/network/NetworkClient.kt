package com.example.playlistmaker.data.network


import com.example.playlistmaker.data.dto.TrackDto

interface NetworkClient {
    suspend fun searchTracks(query: String): List<TrackDto>

    companion object {
        fun create(): NetworkClient {
            return RetrofitNetworkClient()
        }
    }
}