package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.repository.SettingsRepository

class UpdateThemeSettingsUseCase(private val settingsRepository: SettingsRepository) {
    fun execute(darkThemeEnabled: Boolean) = settingsRepository.updateThemeSettings(darkThemeEnabled)
}