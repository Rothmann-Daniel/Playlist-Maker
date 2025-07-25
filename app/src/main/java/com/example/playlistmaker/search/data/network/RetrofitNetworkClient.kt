package com.example.playlistmaker.search.data.network

import com.example.playlistmaker.search.data.dto.TrackDto

class RetrofitNetworkClient(
    private val api: iTunesAPI
) : NetworkClient {
    override suspend fun searchTracks(query: String): List<TrackDto> {
        return try {
            api.search(query).body()?.results ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}