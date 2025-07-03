package com.example.playlistmaker.presentation.settings


import android.app.Application
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.data.repository.SettingsRepositoryImpl


const val SHARED_PREFS = "shared_prefs"

class App : Application() {
    companion object {
        private var _instance: App? = null
        val instance: App
            get() = _instance ?: throw IllegalStateException("Application not initialized")
    }

    lateinit var sharedPrefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        _instance = this
        sharedPrefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)

        // Инициализация темы
        if (!sharedPrefs.contains(SettingsRepositoryImpl.THEMES_KEY)) {
            val isSystemDark = (resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            sharedPrefs.edit().putBoolean(SettingsRepositoryImpl.THEMES_KEY, isSystemDark).apply()
        }

        val darkTheme = sharedPrefs.getBoolean(SettingsRepositoryImpl.THEMES_KEY, false)
        AppCompatDelegate.setDefaultNightMode(
            if (darkTheme) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}