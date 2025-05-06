package com.example.playlistmaker.network

import com.example.playlistmaker.model.TrackResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface iTunesAPI {

    @GET("/search?entity=song")
    fun search(@Query("term") text: String): Call<TrackResponse>
}