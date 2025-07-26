package com.example.playlistmaker.settings.di

import android.content.Context
import com.example.playlistmaker.settings.data.repository.SettingsNavigatorImpl
import com.example.playlistmaker.settings.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.settings.domain.repository.SettingsNavigator
import com.example.playlistmaker.settings.domain.repository.SettingsRepository
import com.example.playlistmaker.settings.domain.usecase.GetThemeSettingsUseCase
import com.example.playlistmaker.settings.domain.usecase.NavigateUseCase
import com.example.playlistmaker.settings.domain.usecase.UpdateThemeSettingsUseCase
import com.example.playlistmaker.settings.ui.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val settingsModule = module {
    // Репозиторий
    single<SettingsRepository> {
        SettingsRepositoryImpl(get()) // Используем общие SharedPreferences
    }

    // Навигатор (исправленная строка)
    single<SettingsNavigator> { SettingsNavigatorImpl(androidContext()) }

    // UseCases
    factory { GetThemeSettingsUseCase(get()) }
    factory { UpdateThemeSettingsUseCase(get()) }
    factory { NavigateUseCase(get()) }

    // ViewModel
    viewModel {
        SettingsViewModel(
            getThemeSettingsUseCase = get(),
            updateThemeSettingsUseCase = get(),
            navigateUseCase = get()
        )
    }
}