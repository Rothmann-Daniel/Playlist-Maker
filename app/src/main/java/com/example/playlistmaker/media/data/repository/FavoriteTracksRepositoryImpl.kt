package com.example.playlistmaker.media.data.repository

import com.example.playlistmaker.media.data.db.AppDatabase
import com.example.playlistmaker.media.data.db.converters.TrackDbConverter
import com.example.playlistmaker.media.domain.repository.FavoriteTracksRepository
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteTracksRepositoryImpl(
    private val database: AppDatabase
) : FavoriteTracksRepository {

    private val trackDao = database.trackDao()

    override suspend fun addTrackToFavorites(track: Track) {
        val entity = TrackDbConverter.mapTrackToEntity(track)
        trackDao.insertTrack(entity)
    }

    override suspend fun removeTrackFromFavorites(track: Track) {
        trackDao.deleteTrack(track.trackId)
    }

    override fun getAllFavoriteTracks(): Flow<List<Track>> {
        return trackDao.getAll()
            .map { entities ->
                entities.map { TrackDbConverter.mapEntityToTrack(it) }
            }
    }
}