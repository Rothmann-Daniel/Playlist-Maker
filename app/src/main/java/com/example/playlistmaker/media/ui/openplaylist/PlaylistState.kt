package com.example.playlistmaker.media.ui.openplaylist

import com.example.playlistmaker.media.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.Track

sealed interface PlaylistState {
    object Loading : PlaylistState
    data class Content(
        val playlist: Playlist,
        val tracks: List<Track>,
        val totalDuration: String,
        val tracksCount: String
    ) : PlaylistState
    data class Error(val message: String) : PlaylistState
}