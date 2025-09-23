package com.example.playlistmaker.player.data.repository

import android.media.MediaPlayer
import com.example.playlistmaker.player.domain.repository.AudioPlayerRepository
import com.example.playlistmaker.player.domain.repository.MediaPlayerProvider
import com.example.playlistmaker.player.domain.model.AudioPlayerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume

class AudioPlayerRepositoryImpl(
    private val mediaPlayerProvider: MediaPlayerProvider
) : AudioPlayerRepository {

    private var mediaPlayer: MediaPlayer? = null
    private val _playerState = MutableStateFlow<AudioPlayerState>(AudioPlayerState.Idle)
    private var lastKnownPosition: Long = 0L // Сохраняем последнюю позицию

    override suspend fun prepare(url: String): Flow<AudioPlayerState> = flow {
        try {
            _playerState.value = AudioPlayerState.Preparing
            emit(AudioPlayerState.Preparing)

            mediaPlayer?.release()
            mediaPlayer = mediaPlayerProvider.createMediaPlayer()

            val prepared = suspendCancellableCoroutine<Boolean> { cont ->
                mediaPlayer?.apply {
                    setDataSource(url)
                    setOnPreparedListener {
                        cont.resume(true)
                    }
                    setOnErrorListener { _, what, extra ->
                        cont.resume(false)
                        true
                    }
                    setOnCompletionListener {
                        lastKnownPosition = 0L // При завершении сбрасываем позицию
                        _playerState.value = AudioPlayerState.Completed
                    }
                    prepareAsync()
                }
            }

            if (prepared) {
                lastKnownPosition = 0L // Сбрасываем позицию при успешной подготовке
                _playerState.value = AudioPlayerState.Prepared
                emit(AudioPlayerState.Prepared)
            } else {
                _playerState.value = AudioPlayerState.Error("Failed to prepare audio")
                emit(AudioPlayerState.Error("Failed to prepare audio"))
            }

        } catch (e: IOException) {
            val errorState = AudioPlayerState.Error("Failed to set data source: ${e.message}")
            _playerState.value = errorState
            emit(errorState)
        } catch (e: IllegalStateException) {
            val errorState = AudioPlayerState.Error("MediaPlayer error: ${e.message}")
            _playerState.value = errorState
            emit(errorState)
        }
    }

    override suspend fun start() {
        mediaPlayer?.start()
        _playerState.value = AudioPlayerState.Playing
    }

    override suspend fun pause() {
        lastKnownPosition = mediaPlayer?.currentPosition?.toLong() ?: lastKnownPosition
        mediaPlayer?.pause()
        _playerState.value = AudioPlayerState.Paused
    }

    override suspend fun stop() {
        mediaPlayer?.stop()
        lastKnownPosition = 0L // При остановке сбрасываем позицию
        _playerState.value = AudioPlayerState.Paused
    }

    override suspend fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    override fun getCurrentPosition(): Flow<Long> = flow {
        while (_playerState.value == AudioPlayerState.Playing && mediaPlayer?.isPlaying == true) {
            val currentPos = mediaPlayer?.currentPosition?.toLong() ?: 0L
            lastKnownPosition = currentPos
            emit(currentPos)
            delay(300L) // Обновление каждые 300мс согласно ТЗ
        }
    }

    override suspend fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        lastKnownPosition = 0L
        _playerState.value = AudioPlayerState.Idle
    }

    override fun getLastKnownPosition(): Long = lastKnownPosition

    override fun getPlayerStateFlow(): Flow<AudioPlayerState> = _playerState.asStateFlow()
}
