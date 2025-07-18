package com.example.playlistmaker.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.creator.InteractorCreator

class SettingsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(
                InteractorCreator.getThemeSettingsUseCase,
                InteractorCreator.updateThemeSettingsUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}