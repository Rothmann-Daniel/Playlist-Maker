package com.example.playlistmaker.player.data.repository

import android.media.MediaPlayer
import com.example.playlistmaker.player.domain.repository.AudioPlayerRepository
import java.io.IOException

class AudioPlayerRepositoryImpl : AudioPlayerRepository {

    private var mediaPlayer: MediaPlayer? = null

    override fun prepare(
        url: String,
        onPrepared: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener { onPrepared() }
                setOnErrorListener { _, what, extra ->
                    onError("MediaPlayer error: $what, $extra")
                    true
                }
                prepareAsync()
            }
        } catch (e: IOException) {
            onError("Failed to set data source: ${e.message}")
        } catch (e: IllegalStateException) {
            onError("MediaPlayer error: ${e.message}")
        }
    }

    override fun start() {
        mediaPlayer?.start()
    }

    override fun pause() {
        mediaPlayer?.pause()
    }

    override fun stop() {
        mediaPlayer?.stop()
    }

    override fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    override fun getCurrentPosition(): Long = mediaPlayer?.currentPosition?.toLong() ?: 0L

    override fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        mediaPlayer?.setOnCompletionListener {
            listener()
        }
    }
}