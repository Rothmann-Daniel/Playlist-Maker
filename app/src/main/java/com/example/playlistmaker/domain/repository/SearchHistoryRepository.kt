package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.Track

interface SearchHistoryRepository {
    fun addTrack(track: Track)
    fun getHistory(): List<Track>
    fun clearHistory()
}