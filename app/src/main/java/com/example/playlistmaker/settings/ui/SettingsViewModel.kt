package com.example.playlistmaker.settings.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.settings.domain.models.SettingsEvent
import com.example.playlistmaker.settings.domain.usecase.GetThemeSettingsUseCase
import com.example.playlistmaker.settings.domain.usecase.UpdateThemeSettingsUseCase


class SettingsViewModel(
    private val getThemeSettingsUseCase: GetThemeSettingsUseCase,
    private val updateThemeSettingsUseCase: UpdateThemeSettingsUseCase
) : ViewModel() {
    private val _themeState = MutableLiveData<Boolean>()
    val themeState: LiveData<Boolean> = _themeState

    private val _events = MutableLiveData<SettingsEvent?>()
    val events: LiveData<SettingsEvent?> = _events

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

    fun onShareAppClicked() {
        _events.value = SettingsEvent.ShareApp
        _events.value = null // Сбрасываем значение после обработки
    }

    fun onSupportClicked() {
        _events.value = SettingsEvent.ContactSupport
        _events.value = null
    }

    fun onUserAgreementClicked() {
        _events.value = SettingsEvent.OpenUserAgreement
        _events.value = null
    }
}