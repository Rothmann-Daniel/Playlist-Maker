package com.example.playlistmaker.media.ui.mediafragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MediaViewModel : ViewModel() {
    private val _selectedTab = MutableStateFlow(0) // Позиция выбранного таба
    val selectedTab: StateFlow<Int> = _selectedTab

    // Метод для выбора таба
    fun selectTab(position: Int) {
        viewModelScope.launch {
            _selectedTab.emit(position)
        }
    }
}