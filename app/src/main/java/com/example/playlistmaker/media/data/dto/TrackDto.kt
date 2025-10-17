package com.example.playlistmaker.media.data.dto

/**
 * DTO для передачи данных трека между слоями Data
 */
data class TrackDto(
    val trackId: Int,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?,
    val previewUrl: String?
)

/**
 * DTO для создания плейлиста
 */
data class CreatePlaylistDto(
    val name: String,
    val description: String?,
    val coverImageUri: android.net.Uri?
)

/**
 * DTO для обновления плейлиста
 */
data class UpdatePlaylistDto(
    val playlistId: Long,
    val name: String,
    val description: String?,
    val coverImagePath: String?
)