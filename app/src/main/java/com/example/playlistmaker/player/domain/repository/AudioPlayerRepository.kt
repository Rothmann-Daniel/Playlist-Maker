package com.example.playlistmaker.player.domain.repository

interface AudioPlayerRepository {
    fun prepare(
        url: String,
        onPrepared: () -> Unit,
        onError: (String) -> Unit
    )
    fun start()
    fun pause()
    fun stop()
    fun isPlaying(): Boolean
    fun getCurrentPosition(): Long
    fun release()
    fun setOnCompletionListener(listener: () -> Unit)
}