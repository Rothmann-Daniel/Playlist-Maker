package com.example.playlistmaker.player.domain.usecase

import com.example.playlistmaker.player.domain.repository.AudioPlayerRepository

class PrepareAudioUseCase(private val repository: AudioPlayerRepository) {
    fun execute(
        url: String,
        onPrepared: () -> Unit,
        onError: (String) -> Unit
    ) = repository.prepare(url, onPrepared, onError)
}