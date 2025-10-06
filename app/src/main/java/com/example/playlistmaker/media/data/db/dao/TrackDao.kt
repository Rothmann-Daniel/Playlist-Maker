package com.example.playlistmaker.media.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.playlistmaker.media.data.db.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Query("DELETE FROM favorite_tracks_table WHERE trackId = :trackId")
    suspend fun deleteTrack(trackId: Int): Int

    @Query("SELECT * FROM favorite_tracks_table ORDER BY addedTimestamp DESC")
    fun getAll(): Flow<List<TrackEntity>>

    @Query("SELECT trackId FROM favorite_tracks_table")
    fun getFavoriteTrackIds(): Flow<List<Int>>
}