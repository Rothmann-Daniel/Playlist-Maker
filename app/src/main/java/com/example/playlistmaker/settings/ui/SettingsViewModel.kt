package com.example.playlistmaker.settings.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.settings.domain.models.SingleLiveEvent
import com.example.playlistmaker.settings.domain.repository.SettingsNavigator
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

    private val _navigationResult = SingleLiveEvent<SettingsNavigator.NavigationResult>()
    val navigationResult: LiveData<SettingsNavigator.NavigationResult> = _navigationResult

    private val _toastMessage = SingleLiveEvent<String>()
    val toastMessage: LiveData<String> = _toastMessage

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
        context: Context,
        message: String,
        shareTitle: String,
        noAppMessage: String,
        errorMessage: String
    ) {
        val result = navigateUseCase.shareApp(
            context = context,
            message = message,
            shareTitle = shareTitle,
            noAppMessage = noAppMessage,
            errorMessage = errorMessage
        )
        _navigationResult.postValue(result)
    }

    fun onSupportClicked(
        context: Context,
        email: String,
        subject: String,
        body: String,
        chooseEmailAppText: String,
        noEmailAppMessage: String,
        errorMessage: String
    ) {
        val result = navigateUseCase.contactSupport(
            context = context,
            email = email,
            subject = subject,
            body = body,
            chooseEmailAppText = chooseEmailAppText,
            noEmailAppMessage = noEmailAppMessage,
            errorMessage = errorMessage
        )
        _navigationResult.postValue(result)
    }

    fun onUserAgreementClicked(
        context: Context,
        url: String,
        noBrowserMessage: String,
        errorMessage: String
    ) {
        val result = navigateUseCase.openUserAgreement(
            context = context,
            url = url,
            noBrowserMessage = noBrowserMessage,
            errorMessage = errorMessage
        )
        _navigationResult.postValue(result)
    }
}