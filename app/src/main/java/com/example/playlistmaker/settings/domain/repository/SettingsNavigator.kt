package com.example.playlistmaker.settings.domain.repository

import android.content.Context

interface SettingsNavigator {


    sealed class NavigationResult {
        object Success : NavigationResult()
        data class Error(val message: String) : NavigationResult()
        data class NoAppFound(val message: String) : NavigationResult()
    }

    fun shareApp(
        context: Context,
        message: String,
        shareTitle: String,
        noAppMessage: String,
        errorMessage: String
    ) : NavigationResult

    fun contactSupport(
        context: Context,
        email: String,
        subject: String,
        body: String,
        chooseEmailAppText: String,
        noEmailAppMessage: String,
        errorMessage: String
    ) : NavigationResult

    fun openUserAgreement(
        context: Context,
        url: String,
        noBrowserMessage: String,
        errorMessage: String
    ) : NavigationResult
}