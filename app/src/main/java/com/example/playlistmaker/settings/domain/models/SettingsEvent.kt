package com.example.playlistmaker.settings.domain.models

sealed class SettingsEvent {
    object ShareApp : SettingsEvent()
    object ContactSupport : SettingsEvent()
    object OpenUserAgreement : SettingsEvent()
}