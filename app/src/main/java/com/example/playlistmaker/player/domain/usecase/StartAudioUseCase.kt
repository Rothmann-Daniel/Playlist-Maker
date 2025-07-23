package com.example.playlistmaker.player.domain.usecase

import com.example.playlistmaker.player.domain.repository.AudioPlayerRepository

class StartAudioUseCase(private val repository: AudioPlayerRepository) {
    fun execute() = repository.start()
}