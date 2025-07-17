package com.example.playlistmaker.settings.domain.usecase

import com.example.playlistmaker.settings.domain.repository.SettingsRepository

class UpdateThemeSettingsUseCase(private val settingsRepository: SettingsRepository) {
    fun execute(darkThemeEnabled: Boolean) = settingsRepository.updateThemeSettings(darkThemeEnabled)
}