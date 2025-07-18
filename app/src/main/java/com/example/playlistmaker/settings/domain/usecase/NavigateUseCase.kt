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
    ) {
        settingsNavigator.shareApp(context, message, shareTitle, noAppMessage, errorMessage)
    }

    fun contactSupport(
        context: Context,
        email: String,
        subject: String,
        body: String,
        chooseEmailAppText: String,
        noEmailAppMessage: String,
        errorMessage: String
    ) {
        settingsNavigator.contactSupport(
            context,
            email,
            subject,
            body,
            chooseEmailAppText,
            noEmailAppMessage,
            errorMessage
        )
    }

    fun openUserAgreement(
        context: Context,
        url: String,
        noBrowserMessage: String,
        errorMessage: String
    ) {
        settingsNavigator.openUserAgreement(context, url, noBrowserMessage, errorMessage)
    }
}