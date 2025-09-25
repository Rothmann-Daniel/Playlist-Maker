package com.example.playlistmaker.player.domain.model

sealed class AudioPlayerState {
    object Idle : AudioPlayerState()
    object Preparing : AudioPlayerState()
    object Prepared : AudioPlayerState()
    object Playing : AudioPlayerState()
    object Paused : AudioPlayerState()
    object Completed : AudioPlayerState()
    data class Error(val message: String) : AudioPlayerState()
}