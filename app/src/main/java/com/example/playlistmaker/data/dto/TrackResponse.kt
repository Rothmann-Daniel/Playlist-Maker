package com.example.playlistmaker.data.dto

import com.example.playlistmaker.domain.models.Track

data class TrackResponse(
    val resultCount: Int,
    val results: List<Track>)

