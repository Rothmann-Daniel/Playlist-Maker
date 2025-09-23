package com.example.playlistmaker.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.player.domain.usecase.AudioPlayerInteractor
import com.example.playlistmaker.player.domain.model.AudioPlayerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*

class AudioPlayerViewModel(
    private val audioPlayerInteractor: AudioPlayerInteractor
) : ViewModel() {

    // Конвертируем AudioPlayerState в UI состояния
    sealed class PlayerState {
        object Preparing : PlayerState()
        object Prepared : PlayerState()
        object Playing : PlayerState()
        object Paused : PlayerState()
        data class Error(val message: String) : PlayerState()
    }

    private val _playerState = MutableLiveData<PlayerState>(PlayerState.Paused)
    val playerState: LiveData<PlayerState> = _playerState

    private val _currentPosition = MutableLiveData<String>("00:00")
    val currentPosition: LiveData<String> = _currentPosition

    // Job для debounce кликов
    private var clickDebounceJob: Job? = null

    fun preparePlayer(url: String) {
        // Используем viewModelScope для запуска корутин и работы с Flow
        audioPlayerInteractor.prepareAudio(url)
            .onEach { state ->
                _playerState.postValue(mapToUiState(state))
            }
            .catch { error ->
                _playerState.postValue(PlayerState.Error(error.message ?: "Unknown error"))
            }
            .launchIn(viewModelScope)

        // Подписываемся на прогресс воспроизведения
        audioPlayerInteractor.getPlaybackProgress()
            .onEach { position ->
                _currentPosition.postValue(formatTime(position))
            }
            .catch { error ->
                // Логируем ошибку, но не прерываем работу
                _currentPosition.postValue("00:00")
            }
            .launchIn(viewModelScope)

        // Подписываемся на изменения состояния плеера
        audioPlayerInteractor.getPlayerState()
            .onEach { state ->
                _playerState.postValue(mapToUiState(state))
                when (state) {
                    is AudioPlayerState.Completed -> {
                        // При завершении трека устанавливаем 00:00
                        _currentPosition.postValue("00:00")
                    }
                    is AudioPlayerState.Paused,
                    is AudioPlayerState.Prepared -> {
                        // При паузе или подготовке не сбрасываем позицию
                    }
                    else -> {
                        // Для других состояний ничего дополнительно не делаем
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    // Debounce для кликов по кнопке play/pause (используем корутины)
    fun togglePlayPause() {
        clickDebounceJob?.cancel()
        clickDebounceJob = viewModelScope.launch {
            delay(CLICK_DEBOUNCE_DELAY_MS) // Простая реализация debounce с отменой Job

            when {
                audioPlayerInteractor.isAudioPlaying() -> {
                    pause()
                }
                _playerState.value == PlayerState.Prepared ||
                        _playerState.value == PlayerState.Paused -> {
                    play()
                }
            }
        }
    }

    private fun play() {
        viewModelScope.launch {
            audioPlayerInteractor.startAudio()
            _playerState.postValue(PlayerState.Playing)
        }
    }

    fun pause() {
        viewModelScope.launch {
            audioPlayerInteractor.pauseAudio()
            _playerState.postValue(PlayerState.Paused)
        }
    }

    fun stop() {
        viewModelScope.launch {
            audioPlayerInteractor.stopAudio()
            _playerState.postValue(PlayerState.Paused)
            _currentPosition.postValue("00:00")
        }
    }



    private fun mapToUiState(state: AudioPlayerState): PlayerState {
        return when (state) {
            is AudioPlayerState.Preparing -> PlayerState.Preparing
            is AudioPlayerState.Prepared -> PlayerState.Prepared
            is AudioPlayerState.Playing -> PlayerState.Playing
            is AudioPlayerState.Paused -> PlayerState.Paused
            is AudioPlayerState.Error -> PlayerState.Error(state.message)
            else -> PlayerState.Paused
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            audioPlayerInteractor.release()
        }
        clickDebounceJob?.cancel()
    }

    fun formatTime(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY_MS = 300L // Debounce для кликов
    }
}