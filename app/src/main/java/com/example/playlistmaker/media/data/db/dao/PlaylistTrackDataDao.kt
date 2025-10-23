package com.example.playlistmaker.media.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.playlistmaker.media.data.db.entity.PlaylistTrackDataEntity

@Dao
interface PlaylistTrackDataDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: PlaylistTrackDataEntity): Long

    @Query("SELECT * FROM playlist_track_data WHERE playlistId = :playlistId ORDER BY addedTimestamp DESC")
    suspend fun getTracksByPlaylistId(playlistId: Long): List<PlaylistTrackDataEntity>

    @Query("SELECT trackId FROM playlist_track_data WHERE playlistId = :playlistId")
    suspend fun getTrackIdsByPlaylistId(playlistId: Long): List<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_track_data WHERE playlistId = :playlistId AND trackId = :trackId)")
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Int): Boolean

    @Query("DELETE FROM playlist_track_data WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Int)

    @Query("SELECT COUNT(*) FROM playlist_track_data WHERE playlistId = :playlistId")
    suspend fun getTracksCount(playlistId: Long): Int

    @Query("DELETE FROM playlist_track_data WHERE playlistId = :playlistId")
    suspend fun deleteAllTracksFromPlaylist(playlistId: Long)

    // Получение всех треков
    @Query("SELECT * FROM playlist_track_data")
    suspend fun getAllTracks(): List<PlaylistTrackDataEntity>
}