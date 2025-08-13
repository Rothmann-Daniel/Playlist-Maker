package com.example.playlistmaker.media.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlaylistsViewModel : ViewModel() {

    // Toast создан для тренировки работы с LiveData MVVM

    private val _toastMessage = MutableLiveData<String?>()// Приватное поле для хранения сообщения
    val toastMessage: LiveData<String?> = _toastMessage // Публичное поле для доступа к сообщению LiveData для наблюдения
    fun createPlayList() {
        _toastMessage.value = "Плейлист будет создан, но позже"  // Устанавливаем значение
        // Здесь логика создания плейлиста

    }

    // Добавляем метод для сброса сообщения
    fun resetToastMessage() {
        _toastMessage.value = null
    }
}