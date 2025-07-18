package com.example.playlistmaker.settings.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.settings.domain.models.SingleLiveEvent
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

    sealed class NavigationEvent {
        data class ShareApp(
            val message: String,
            val shareTitle: String,
            val noAppMessage: String,
            val errorMessage: String
        ) : NavigationEvent()

        data class ContactSupport(
            val email: String,
            val subject: String,
            val body: String,
            val chooseEmailAppText: String,
            val noEmailAppMessage: String,
            val errorMessage: String
        ) : NavigationEvent()

        data class OpenUserAgreement(
            val url: String,
            val noBrowserMessage: String,
            val errorMessage: String
        ) : NavigationEvent()
    }

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

    fun onShareAppClicked(
        message: String,
        shareTitle: String,
        noAppMessage: String,
        errorMessage: String
    ) {
        _navigationEvent.value = NavigationEvent.ShareApp(
            message = message,
            shareTitle = shareTitle,
            noAppMessage = noAppMessage,
            errorMessage = errorMessage
        )
    }

    fun onSupportClicked(
        email: String,
        subject: String,
        body: String,
        chooseEmailAppText: String,
        noEmailAppMessage: String,
        errorMessage: String
    ) {
        _navigationEvent.value = NavigationEvent.ContactSupport(
            email = email,
            subject = subject,
            body = body,
            chooseEmailAppText = chooseEmailAppText,
            noEmailAppMessage = noEmailAppMessage,
            errorMessage = errorMessage
        )
    }

    fun onUserAgreementClicked(
        url: String,
        noBrowserMessage: String,
        errorMessage: String
    ) {
        _navigationEvent.value = NavigationEvent.OpenUserAgreement(
            url = url,
            noBrowserMessage = noBrowserMessage,
            errorMessage = errorMessage
        )
    }
}