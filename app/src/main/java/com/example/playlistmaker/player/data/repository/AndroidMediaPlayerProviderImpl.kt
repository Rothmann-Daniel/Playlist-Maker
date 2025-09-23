package com.example.playlistmaker.player.data.repository

import android.media.MediaPlayer
import com.example.playlistmaker.player.domain.repository.MediaPlayerProvider


class AndroidMediaPlayerProviderImpl : MediaPlayerProvider {
    override fun createMediaPlayer(): MediaPlayer {
        return MediaPlayer()
    }
}
