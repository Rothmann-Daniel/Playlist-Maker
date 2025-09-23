package com.example.playlistmaker.player.domain.repository

import com.example.playlistmaker.player.domain.model.AudioPlayerState
import kotlinx.coroutines.flow.Flow

interface AudioPlayerRepository {
    suspend fun prepare(url: String): Flow<AudioPlayerState>
    suspend fun start()
    suspend fun pause()
    suspend fun stop()
    suspend fun isPlaying(): Boolean
    fun getCurrentPosition(): Flow<Long>
    fun getLastKnownPosition(): Long
    suspend fun release()
    fun getPlayerStateFlow(): Flow<AudioPlayerState>
}
