package com.example.playlistmaker.player.domain.usecase

import com.example.playlistmaker.player.domain.repository.AudioPlayerRepository

class GetAudioPositionUseCase(private val repository: AudioPlayerRepository) {
    fun execute(): Int = repository.getCurrentPosition()
}
