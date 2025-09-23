package com.example.playlistmaker.player.domain.usecase

import com.example.playlistmaker.player.domain.repository.AudioPlayerRepository
import com.example.playlistmaker.player.domain.model.AudioPlayerState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class AudioPlayerInteractor(
    private val repository: AudioPlayerRepository
) {

    fun prepareAudio(url: String): Flow<AudioPlayerState> =
        flow { emitAll(repository.prepare(url)) }

    suspend fun startAudio() = repository.start()

    suspend fun pauseAudio() = repository.pause()

    suspend fun stopAudio() = repository.stop()

    suspend fun isAudioPlaying(): Boolean = repository.isPlaying()

    fun getCurrentPosition(): Flow<Long> = repository.getCurrentPosition()
        .takeWhile { repository.isPlaying() } // Останавливаем поток когда воспроизведение останавливается

    fun getPlayerState(): Flow<AudioPlayerState> = repository.getPlayerStateFlow()

    suspend fun release() = repository.release()

    // Функция для отслеживания прогресса только во время воспроизведения
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPlaybackProgress(): Flow<Long> = repository.getPlayerStateFlow()
        .flatMapLatest { state ->
            when (state) {
                is AudioPlayerState.Playing -> {
                    repository.getCurrentPosition()
                }
                is AudioPlayerState.Paused -> {
                    flowOf(repository.getLastKnownPosition()) // Показываем последнюю позицию при паузе
                }
                is AudioPlayerState.Completed -> {
                    flowOf(0L) // При завершении трека возвращаем 00:00
                }
                is AudioPlayerState.Prepared -> {
                    flowOf(0L) // При подготовке показываем 00:00
                }
                else -> {
                    flowOf(0L) // Для всех других состояний возвращаем 0
                }
            }
        }
}