package com.example.playlistmaker.player.domain.repository

import android.media.MediaPlayer


interface MediaPlayerProvider {
    fun createMediaPlayer(): MediaPlayer
}