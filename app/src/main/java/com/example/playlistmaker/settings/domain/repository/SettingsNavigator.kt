package com.example.playlistmaker.settings.domain.repository


sealed class NavigationEvent {
    data class ShareApp(val message: String) : NavigationEvent()
    data class ContactSupport(
        val email: String,
        val subject: String,
        val body: String
    ) : NavigationEvent()
    data class OpenUserAgreement(val url: String) : NavigationEvent()
}

interface SettingsNavigator {
    fun navigate(event: NavigationEvent)
}