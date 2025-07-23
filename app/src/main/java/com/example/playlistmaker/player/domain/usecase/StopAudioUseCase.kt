package com.example.playlistmaker.player.domain.usecase

import com.example.playlistmaker.player.domain.repository.AudioPlayerRepository

class StopAudioUseCase(private val repository: AudioPlayerRepository) {
    fun execute() = repository.stop()
}