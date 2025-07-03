package com.example.playlistmaker.data.network

import com.example.playlistmaker.data.dto.TrackDto
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitNetworkClient : NetworkClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(iTunesAPI::class.java)

    override suspend fun searchTracks(query: String): List<TrackDto> {
        val response = api.search(query)
        return response.body()?.results ?: emptyList()
    }
}