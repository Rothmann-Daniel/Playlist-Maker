package com.example.playlistmaker.util


import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.player.di.playerModule
import com.example.playlistmaker.search.di.networkModule
import com.example.playlistmaker.search.di.searchModule
import com.example.playlistmaker.settings.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.settings.di.settingsModule
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val SHARED_PREFS = "shared_prefs"

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(
                networkModule,
                searchModule,
                settingsModule,
                playerModule,
                module {
                    single { Gson() }
                    single(named("search_history_prefs")) {
                        getSharedPreferences("search_history", Context.MODE_PRIVATE)
                    }
                }
            )
        }

        // Инициализация темы
        val sharedPrefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        if (!sharedPrefs.contains(SettingsRepositoryImpl.THEMES_KEY)) {
            val isSystemDark = (resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            sharedPrefs.edit().putBoolean(SettingsRepositoryImpl.THEMES_KEY, isSystemDark).apply()
        }
        AppCompatDelegate.setDefaultNightMode(
            if (sharedPrefs.getBoolean(SettingsRepositoryImpl.THEMES_KEY, false)) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}
