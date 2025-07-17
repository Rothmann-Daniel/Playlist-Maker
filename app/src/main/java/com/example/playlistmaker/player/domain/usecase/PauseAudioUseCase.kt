package com.example.playlistmaker.player.domain.usecase

import com.example.playlistmaker.player.domain.repository.AudioPlayerRepository

class PauseAudioUseCase(private val repository: AudioPlayerRepository) {
    fun execute() = repository.pause()
}