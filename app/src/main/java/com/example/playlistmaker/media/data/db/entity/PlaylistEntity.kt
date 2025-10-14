package com.example.playlistmaker.media.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "playlists_table")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val playlistId: Long = 0,
    val name: String,
    val description: String?,
    val coverImagePath: String?,
    val trackIdsJson: String, // Список ID треков в формате JSON
    val tracksCount: Int = 0,
    val createdTimestamp: Long = System.currentTimeMillis()
)