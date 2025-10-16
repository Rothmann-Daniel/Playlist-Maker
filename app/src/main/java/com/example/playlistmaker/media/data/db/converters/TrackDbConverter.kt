package com.example.playlistmaker.media.data.db.converters

import com.example.playlistmaker.media.data.db.entity.TrackEntity
import com.example.playlistmaker.search.domain.model.Track

/**
 * Конвертер для избранных треков
 */
object TrackDbConverter {

    fun mapTrackToEntity(track: Track): TrackEntity {
        return TrackEntity(
            trackId = track.trackId,
            artworkUrl100 = track.artworkUrl100,
            trackName = track.trackName,
            artistName = track.artistName,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            trackTimeMillis = track.trackTimeMillis,
            previewUrl = track.previewUrl,
            isFavorite = track.isFavorite,
            addedTimestamp = System.currentTimeMillis()
        )
    }

    fun mapEntityToTrack(entity: TrackEntity): Track {
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
            isFavorite = entity.isFavorite
        )
    }
}