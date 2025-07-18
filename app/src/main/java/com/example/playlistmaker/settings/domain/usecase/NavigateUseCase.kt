package com.example.playlistmaker.settings.domain.usecase

import android.content.Context
import com.example.playlistmaker.settings.domain.repository.SettingsNavigator

class NavigateUseCase(private val settingsNavigator: SettingsNavigator) {

    fun shareApp(
        context: Context,
        message: String,
        shareTitle: String,
        noAppMessage: String,
        errorMessage: String
    ): SettingsNavigator.NavigationResult {
        return settingsNavigator.shareApp(
            context = context,
            message = message,
            shareTitle = shareTitle,
            noAppMessage = noAppMessage,
            errorMessage = errorMessage
        )
    }

    fun contactSupport(
        context: Context,
        email: String,
        subject: String,
        body: String,
        chooseEmailAppText: String,
        noEmailAppMessage: String,
        errorMessage: String
    ): SettingsNavigator.NavigationResult {
        return settingsNavigator.contactSupport(
            context = context,
            email = email,
            subject = subject,
            body = body,
            chooseEmailAppText = chooseEmailAppText,
            noEmailAppMessage = noEmailAppMessage,
            errorMessage = errorMessage
        )
    }

    fun openUserAgreement(
        context: Context,
        url: String,
        noBrowserMessage: String,
        errorMessage: String
    ): SettingsNavigator.NavigationResult {
        return settingsNavigator.openUserAgreement(
            context = context,
            url = url,
            noBrowserMessage = noBrowserMessage,
            errorMessage = errorMessage
        )
    }
}