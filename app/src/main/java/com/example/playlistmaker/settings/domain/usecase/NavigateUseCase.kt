package com.example.playlistmaker.settings.domain.usecase

import android.content.Context
import com.example.playlistmaker.settings.domain.repository.SettingsNavigator

class NavigateUseCase(private val settingsNavigator: SettingsNavigator) {
    fun shareApp(context: Context, message: String) {
        settingsNavigator.shareApp(context, message)
    }

    fun contactSupport(context: Context, email: String, subject: String, body: String) {
        settingsNavigator.contactSupport(context, email, subject, body)
    }

    fun openUserAgreement(context: Context, url: String) {
        settingsNavigator.openUserAgreement(context, url)
    }
}