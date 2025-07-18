package com.example.playlistmaker.settings.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.settings.domain.usecase.GetThemeSettingsUseCase
import com.example.playlistmaker.settings.domain.usecase.UpdateThemeSettingsUseCase


class SettingsViewModel(
    private val getThemeSettingsUseCase: GetThemeSettingsUseCase,
    private val updateThemeSettingsUseCase: UpdateThemeSettingsUseCase
) : ViewModel() {
    private val _themeState = MutableLiveData<Boolean>()
    val themeState: LiveData<Boolean> = _themeState

    init {
        loadTheme()
    }

    private fun loadTheme() {
        _themeState.value = getThemeSettingsUseCase.execute()
    }

    fun updateThemeSettings(darkThemeEnabled: Boolean) {
        updateThemeSettingsUseCase.execute(darkThemeEnabled)
        _themeState.value = darkThemeEnabled
    }
}