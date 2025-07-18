package com.example.playlistmaker.settings.domain.usecase

import com.example.playlistmaker.settings.domain.repository.NavigationEvent
import com.example.playlistmaker.settings.domain.repository.SettingsNavigator


class NavigateUseCase(private val navigator: SettingsNavigator) {
    fun navigate(event: NavigationEvent) = navigator.navigate(event)
}