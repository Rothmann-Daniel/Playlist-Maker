package com.example.playlistmaker


import android.app.Application
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

const val SHARED_PREFS = "shared_prefs"
const val THEMES_KEY = "themes_key"

class App : Application() {

    var darkTheme = false
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        sharedPrefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        // Проверяем, есть ли сохраненное значение темы
        if (!sharedPrefs.contains(THEMES_KEY)) {
            // Если нет - определяем системную тему
            val isSystemDark = (resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            darkTheme = isSystemDark
            // Сохраняем системную тему
            sharedPrefs.edit().putBoolean(THEMES_KEY, darkTheme).apply()
        } else {
            // Если есть - берем сохраненное значение
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