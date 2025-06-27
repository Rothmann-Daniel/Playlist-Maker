package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.repository.SettingsRepository

class GetThemeSettingsUseCase(private val settingsRepository: SettingsRepository) {
    fun execute(): Boolean = settingsRepository.getThemeSettings()
}