package com.example.playlistmaker.settings.domain.usecase

import com.example.playlistmaker.settings.domain.repository.SettingsRepository

class GetThemeSettingsUseCase(private val settingsRepository: SettingsRepository) {
    fun execute(): Boolean = settingsRepository.getThemeSettings()
}