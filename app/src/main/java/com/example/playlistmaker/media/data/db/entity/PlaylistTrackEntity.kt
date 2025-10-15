package com.example.playlistmaker.media.data.db.entity

import androidx.room.Entity

@Entity(
    tableName = "playlist_tracks_table",
    primaryKeys = ["playlistId", "trackId"]
)
data class PlaylistTrackEntity(
    val playlistId: Long,
    val trackId: Int,
    val addedTimestamp: Long = System.currentTimeMillis()
)