package com.example.playlistmaker.search.data.repository

import android.content.SharedPreferences
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.domain.repository.SearchHistoryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class SearchHistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : SearchHistoryRepository {

    private companion object {
        const val KEY_HISTORY = "search_history"
        const val MAX_HISTORY_SIZE = 10
    }

    override suspend fun addTrack(track: Track) {
        val history = getHistory().first().toMutableList()
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)
        if (history.size > MAX_HISTORY_SIZE) {
            history.subList(MAX_HISTORY_SIZE, history.size).clear()
        }
        saveHistory(history)
    }

    override fun getHistory(): Flow<List<Track>> = flow {
        val json = sharedPreferences.getString(KEY_HISTORY, null)
        val history = if (json != null) {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson<List<Track>>(json, type) ?: emptyList()
        } else {
            emptyList()
        }
        emit(history)
    }

    override suspend fun clearHistory() {
        sharedPreferences.edit().remove(KEY_HISTORY).apply()
    }

    private fun saveHistory(history: List<Track>) {
        sharedPreferences.edit()
            .putString(KEY_HISTORY, gson.toJson(history))
            .apply()
    }
}