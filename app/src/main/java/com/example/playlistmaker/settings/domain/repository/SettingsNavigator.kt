package com.example.playlistmaker.settings.domain.repository

import android.content.Context

interface SettingsNavigator {
    fun shareApp(
        context: Context,
        message: String,
        shareTitle: String,
        noAppMessage: String,
        errorMessage: String
    )

    fun contactSupport(
        context: Context,
        email: String,
        subject: String,
        body: String,
        chooseEmailAppText: String,
        noEmailAppMessage: String,
        errorMessage: String
    )

    fun openUserAgreement(
        context: Context,
        url: String,
        noBrowserMessage: String,
        errorMessage: String
    )
}