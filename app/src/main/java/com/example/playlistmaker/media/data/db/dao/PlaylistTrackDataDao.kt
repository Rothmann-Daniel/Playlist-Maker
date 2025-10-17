package com.example.playlistmaker.media.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.playlistmaker.media.data.db.entity.PlaylistTrackDataEntity

@Dao
interface PlaylistTrackDataDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: PlaylistTrackDataEntity)

    @Query("SELECT * FROM playlist_track_data WHERE trackId IN (:trackIds)")
    suspend fun getTracksByIds(trackIds: List<Int>): List<PlaylistTrackDataEntity>

    @Query("DELETE FROM playlist_track_data WHERE trackId = :trackId AND trackId NOT IN (SELECT trackId FROM playlist_tracks_table)")
    suspend fun deleteUnusedTrack(trackId: Int)
}