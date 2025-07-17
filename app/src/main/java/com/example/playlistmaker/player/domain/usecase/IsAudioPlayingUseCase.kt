package com.example.playlistmaker.player.domain.usecase

import com.example.playlistmaker.player.domain.repository.AudioPlayerRepository

class IsAudioPlayingUseCase(private val repository: AudioPlayerRepository) {
    fun execute(): Boolean = repository.isPlaying()
}