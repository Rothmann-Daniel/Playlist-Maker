package com.example.playlistmaker.search.domain.repository

import com.example.playlistmaker.search.domain.model.Track


interface TrackRepository {
    suspend fun searchTracks(query: String): List<Track>  // Для поиска треков
    suspend fun addTrackToHistory(track: Track)         // Для добавления в историю
    suspend fun getSearchHistory(): List<Track>         // Для получения истории
    suspend fun clearSearchHistory()                     // Для очистки истории
}