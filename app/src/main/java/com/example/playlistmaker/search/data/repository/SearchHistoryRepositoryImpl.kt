package com.example.playlistmaker.search.data.repository

import android.content.SharedPreferences
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.domain.repository.SearchHistoryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson = Gson()
) : SearchHistoryRepository {

    private companion object {
        const val KEY_HISTORY = "search_history"
        const val MAX_HISTORY_SIZE = 10
    }

    override fun addTrack(track: Track) {
        val history = getHistory().toMutableList()
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)
        if (history.size > MAX_HISTORY_SIZE) {
            history.removeAt(history.lastIndex)
        }
        saveHistory(history)
    }

    override fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(KEY_HISTORY, null)
        return if (json != null) {
            gson.fromJson(json, object : TypeToken<List<Track>>() {}.type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    override fun clearHistory() {
        sharedPreferences.edit().remove(KEY_HISTORY).apply()
    }

    private fun saveHistory(history: List<Track>) {
        sharedPreferences.edit()
            .putString(KEY_HISTORY, gson.toJson(history))
            .apply()
    }
}