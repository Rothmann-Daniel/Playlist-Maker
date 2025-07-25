package com.example.playlistmaker.settings.data.repository

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.settings.domain.repository.SettingsRepository

class SettingsRepositoryImpl(private val sharedPreferences: SharedPreferences) :
    SettingsRepository {

    override fun getThemeSettings(): Boolean {
        return sharedPreferences.getBoolean(THEMES_KEY, false)
    }

    override fun updateThemeSettings(darkThemeEnabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(THEMES_KEY, darkThemeEnabled)
            .apply()
        // Немедленно применяем тему
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    companion object {
        const val THEMES_KEY = "themes_key"
    }
}