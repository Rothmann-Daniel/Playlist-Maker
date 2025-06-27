package com.example.playlistmaker.presentation.settings


import android.app.Application
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

const val SHARED_PREFS = "shared_prefs"
const val THEMES_KEY = "themes_key"

class App : Application() {
    companion object {
        lateinit var instance: App
            private set
    }

    var darkTheme = false
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        instance = this

        sharedPrefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        if (!sharedPrefs.contains(THEMES_KEY)) {
            val isSystemDark = (resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            darkTheme = isSystemDark
            sharedPrefs.edit().putBoolean(THEMES_KEY, darkTheme).apply()
        } else {
            darkTheme = sharedPrefs.getBoolean(THEMES_KEY, false)
        }
        switchTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
        sharedPrefs.edit()
            .putBoolean(THEMES_KEY, darkTheme)
            .apply()
    }
}