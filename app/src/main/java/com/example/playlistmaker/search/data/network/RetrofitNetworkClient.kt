package com.example.playlistmaker.search.data.network

import com.example.playlistmaker.search.data.dto.TrackDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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

    override fun searchTracksFlow(query: String): Flow<List<TrackDto>> = flow {
        try {
            val results = searchTracks(query) // Используем существующий метод
            emit(results)
        } catch (e: Exception) {
            emit(emptyList()) // В случае ошибки возвращаем пустой список
        }
    }
}