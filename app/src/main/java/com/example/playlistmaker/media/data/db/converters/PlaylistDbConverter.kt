package com.example.playlistmaker.media.data.db.converters

import com.example.playlistmaker.media.data.db.entity.PlaylistEntity
import com.example.playlistmaker.media.domain.model.Playlist
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Конвертер для работы с плейлистами
 */
object PlaylistDbConverter {

    fun mapPlaylistToEntity(playlist: Playlist): PlaylistEntity {
        return PlaylistEntity(
            playlistId = playlist.playlistId,
            name = playlist.name,
            description = playlist.description,
            coverImagePath = playlist.coverImagePath,
            tracksCount = playlist.tracksCount,
            createdTimestamp = playlist.createdTimestamp
        )
    }

    fun mapEntityToPlaylist(entity: PlaylistEntity, trackIds: List<Int> = emptyList()): Playlist {
        return Playlist(
            playlistId = entity.playlistId,
            name = entity.name,
            description = entity.description,
            coverImagePath = entity.coverImagePath,
            trackIds = trackIds,
            tracksCount = entity.tracksCount,
            createdTimestamp = entity.createdTimestamp
        )
    }
}