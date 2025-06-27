package com.example.playlistmaker.ui.search

import android.content.SharedPreferences
import com.example.playlistmaker.domain.model.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Deprecated(
    message = "Использется SearchHistoryRepository вместо этого класса",
    replaceWith = ReplaceWith("SearchHistoryRepository"),
    level = DeprecationLevel.ERROR
)

class SearchHistory(private val sharedPreferences: SharedPreferences) {
    private val gson = Gson()
    private val keyHistory = "search_history" // Ключ для хранения в SharedPreferences
    private val maxHistorySize = 10 // Максимальное количество треков в истории

    // Добавить трек в историю
    fun addTrack(track: Track) {
        // Получаем текущую историю
        val history = getHistory().toMutableList()

        // Удаляем трек, если он уже есть в истории (чтобы избежать дублирования)
        history.removeAll { it.trackId == track.trackId }

        // Добавляем новый трек в начало списка
        history.add(0, track)

        // Обрезаем список, если он превышает максимальный размер
        if (history.size > maxHistorySize) {
            history.subList(maxHistorySize, history.size).clear()
        }

        // Сохраняем обновленную историю
        saveHistory(history)
    }

    // Получить всю историю поиска
    fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(keyHistory, null)
        return if (json != null) {
            // Конвертируем JSON обратно в список треков
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    // Очистить всю историю
    fun clearHistory() {
        sharedPreferences.edit()
            .remove(keyHistory)
            .apply()
    }

    // Сохранить историю в SharedPreferences
    private fun saveHistory(history: List<Track>) {
        val json = gson.toJson(history)
        sharedPreferences.edit()
            .putString(keyHistory, json)
            .apply()
    }
}