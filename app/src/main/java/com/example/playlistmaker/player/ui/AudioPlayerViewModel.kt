package com.example.playlistmaker.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.player.domain.usecase.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class AudioPlayerViewModel(
    private val prepareAudioUseCase: PrepareAudioUseCase,
    private val startAudioUseCase: StartAudioUseCase,
    private val pauseAudioUseCase: PauseAudioUseCase,
    private val stopAudioUseCase: StopAudioUseCase,
    private val isAudioPlayingUseCase: IsAudioPlayingUseCase,
    private val getAudioPositionUseCase: GetAudioPositionUseCase,
    private val releasePlayerUseCase: ReleasePlayerUseCase,
    private val setCompletionListenerUseCase: SetCompletionListenerUseCase
) : ViewModel() {

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

    private var progressObserver: Job? = null

    fun preparePlayer(url: String) {
        _playerState.value = PlayerState.Preparing

        // Устанавливаем слушатель завершения воспроизведения
        setCompletionListenerUseCase.execute {
            viewModelScope.launch {
                pause() // Останавливаем воспроизведение при завершении
                _currentPosition.postValue(formatTime(MAX_PREVIEW_DURATION_MS))
                _playerState.postValue(PlayerState.Prepared)
            }
        }

        // Запускаем подготовку аудио
        prepareAudioUseCase.execute(
            url = url,
            onPrepared = {
                _playerState.postValue(PlayerState.Prepared)
                _currentPosition.postValue("00:00") // Сбрасываем позицию при подготовке
            },
            onError = { error ->
                _playerState.postValue(PlayerState.Error(error))
            }
        )
    }

    fun togglePlayPause() {
        when {
            isAudioPlayingUseCase.execute() -> {
                pause()
            }
            _playerState.value == PlayerState.Prepared ||
                    _playerState.value == PlayerState.Paused -> {
                play()
            }
            _playerState.value == PlayerState.Preparing -> {
                // Можно показать Toast или изменить UI, чтобы показать, что плеер еще готовится
            }
        }
    }

    private fun play() {
        startAudioUseCase.execute()
        _playerState.value = PlayerState.Playing
        startProgressTracking()
    }

    fun pause() {
        pauseAudioUseCase.execute()
        _playerState.value = PlayerState.Paused
        stopProgressTracking()
    }

    fun stop() {
        stopAudioUseCase.execute()
        _playerState.value = PlayerState.Paused
        stopProgressTracking()
        _currentPosition.value = "00:00"
    }

    private fun startProgressTracking() {
        stopProgressTracking()
        progressObserver = viewModelScope.launch {
            while (isAudioPlayingUseCase.execute()) {
                _currentPosition.postValue(formatTime(getAudioPositionUseCase.execute()))
                delay(UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun stopProgressTracking() {
        progressObserver?.cancel()
        progressObserver = null
    }



    override fun onCleared() {
        super.onCleared()
        releasePlayerUseCase.execute()
        stopProgressTracking()
    }

    fun formatTime(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
    }

    companion object {
        private const val UPDATE_INTERVAL_MS = 100L
        private const val MAX_PREVIEW_DURATION_MS = 30000L // 30 секунд
    }
}