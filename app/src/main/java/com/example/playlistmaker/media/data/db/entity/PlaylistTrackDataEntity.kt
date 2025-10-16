package com.example.playlistmaker.media.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Отдельная таблица для хранения данных треков в плейлистах
 * (не смешивается с таблицей избранного)
 */
@Entity(tableName = "playlist_track_data")
data class PlaylistTrackDataEntity(
    @PrimaryKey
    val trackId: Int,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?,
    val previewUrl: String?,
    val addedTimestamp: Long = System.currentTimeMillis()
)