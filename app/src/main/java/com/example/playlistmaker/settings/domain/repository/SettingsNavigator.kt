package com.example.playlistmaker.settings.domain.repository

import android.content.Context

interface SettingsNavigator {
    fun shareApp(context: Context, message: String)
    fun contactSupport(context: Context, email: String, subject: String, body: String)
    fun openUserAgreement(context: Context, url: String)
}