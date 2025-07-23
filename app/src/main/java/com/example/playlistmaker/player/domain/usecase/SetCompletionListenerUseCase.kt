package com.example.playlistmaker.player.domain.usecase

import com.example.playlistmaker.player.domain.repository.AudioPlayerRepository

class SetCompletionListenerUseCase(private val repository: AudioPlayerRepository) {
    fun execute(listener: () -> Unit) = repository.setOnCompletionListener(listener)
}