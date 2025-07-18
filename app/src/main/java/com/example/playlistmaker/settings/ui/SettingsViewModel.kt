package com.example.playlistmaker.settings.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.settings.domain.models.SingleLiveEvent
import com.example.playlistmaker.settings.domain.repository.NavigationEvent
import com.example.playlistmaker.settings.domain.usecase.GetThemeSettingsUseCase
import com.example.playlistmaker.settings.domain.usecase.NavigateUseCase
import com.example.playlistmaker.settings.domain.usecase.UpdateThemeSettingsUseCase


class SettingsViewModel(
    private val getThemeSettingsUseCase: GetThemeSettingsUseCase,
    private val updateThemeSettingsUseCase: UpdateThemeSettingsUseCase,
    private val navigateUseCase: NavigateUseCase
) : ViewModel() {

    private val _themeState = MutableLiveData<Boolean>()
    val themeState: LiveData<Boolean> = _themeState

    private val _navigationEvent = SingleLiveEvent<NavigationEvent>()
    val navigationEvent: LiveData<NavigationEvent> = _navigationEvent

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

    fun onShareAppClicked(message: String) {
        _navigationEvent.value = NavigationEvent.ShareApp(message)
    }

    fun onSupportClicked(email: String, subject: String, body: String) {
        _navigationEvent.value = NavigationEvent.ContactSupport(email, subject, body)
    }

    fun onUserAgreementClicked(url: String) {
        _navigationEvent.value = NavigationEvent.OpenUserAgreement(url)
    }
}