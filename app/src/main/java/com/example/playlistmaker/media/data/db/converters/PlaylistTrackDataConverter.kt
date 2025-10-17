package com.example.playlistmaker.media.data.db.converters

import com.example.playlistmaker.media.data.db.entity.PlaylistTrackDataEntity
import com.example.playlistmaker.search.domain.model.Track

/**
 * Конвертер для треков плейлистов работает через промежуточный DTO
 */
object PlaylistTrackDataConverter {

    // Конвертация из Entity в Domain модель
    fun mapEntityToTrack(entity: PlaylistTrackDataEntity, isFavorite: Boolean = false): Track {
        return Track(
            trackId = entity.trackId,
            artworkUrl100 = entity.artworkUrl100,
            trackName = entity.trackName,
            artistName = entity.artistName,
            collectionName = entity.collectionName,
            releaseDate = entity.releaseDate,
            primaryGenreName = entity.primaryGenreName,
            country = entity.country,
            trackTimeMillis = entity.trackTimeMillis,
            previewUrl = entity.previewUrl,
            isFavorite = isFavorite
        )
    }

    // Конвертация из Domain модели в Entity (только внутри Data слоя)
    fun mapTrackToEntity(track: Track): PlaylistTrackDataEntity {
        return PlaylistTrackDataEntity(
            trackId = track.trackId,
            artworkUrl100 = track.artworkUrl100,
            trackName = track.trackName,
            artistName = track.artistName,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            trackTimeMillis = track.trackTimeMillis,
            previewUrl = track.previewUrl
        )
    }

}
