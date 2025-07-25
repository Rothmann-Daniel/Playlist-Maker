package com.example.playlistmaker.search.data.network

import com.example.playlistmaker.search.data.dto.TrackResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface iTunesAPI {
    @GET("/search?entity=song")
    suspend fun search(@Query("term") text: String): Response<TrackResponse>
}