package com.example.playlistmaker.presentation.settings

import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.usecase.GetThemeSettingsUseCase
import com.example.playlistmaker.domain.usecase.UpdateThemeSettingsUseCase

class SettingsViewModel(
    private val getThemeSettingsUseCase: GetThemeSettingsUseCase,
    private val updateThemeSettingsUseCase: UpdateThemeSettingsUseCase
) : ViewModel() {

    fun getThemeSettings(): Boolean {
        return getThemeSettingsUseCase.execute()
    }

    fun updateThemeSettings(darkThemeEnabled: Boolean) {
        updateThemeSettingsUseCase.execute(darkThemeEnabled)
    }
}