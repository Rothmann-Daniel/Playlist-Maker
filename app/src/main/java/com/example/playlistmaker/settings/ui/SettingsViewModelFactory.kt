package com.example.playlistmaker.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.settings.domain.usecase.GetThemeSettingsUseCase
import com.example.playlistmaker.settings.domain.usecase.NavigateUseCase
import com.example.playlistmaker.settings.domain.usecase.UpdateThemeSettingsUseCase

class SettingsViewModelFactory(
    private val getThemeSettingsUseCase: GetThemeSettingsUseCase,
    private val updateThemeSettingsUseCase: UpdateThemeSettingsUseCase,
    private val navigateUseCase: NavigateUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(
                getThemeSettingsUseCase,
                updateThemeSettingsUseCase,
                navigateUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}