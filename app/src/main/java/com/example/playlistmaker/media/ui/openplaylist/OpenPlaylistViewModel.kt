package com.example.playlistmaker.media.ui.openplaylist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.media.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.media.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OpenPlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _state = MutableStateFlow<PlaylistState>(PlaylistState.Loading)
    val state: StateFlow<PlaylistState> = _state.asStateFlow()

    private val _deleteResult = MutableLiveData<DeleteResult?>()
    val deleteResult: LiveData<DeleteResult?> = _deleteResult

    private var currentPlaylistId: Long = 0

    // Plurals функции
    var tracksCountPlurals: ((Int) -> String)? = null
    var minutesCountPlurals: ((Int) -> String)? = null

    fun loadPlaylist(playlistId: Long) {
        currentPlaylistId = playlistId
        _state.value = PlaylistState.Loading

        viewModelScope.launch {
            try {
                val playlist = playlistInteractor.getPlaylistById(playlistId)

                if (playlist == null) {
                    _state.value = PlaylistState.Error("Плейлист не найден")
                    return@launch
                }

                val tracks = playlistInteractor.getTracksForPlaylist(playlistId)
                updateState(playlist, tracks)
            } catch (e: Exception) {
                _state.value = PlaylistState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    fun deleteTrack(trackId: Int) {
        viewModelScope.launch {
            try {
                playlistInteractor.removeTrackFromPlaylist(currentPlaylistId, trackId)
                // Перезагружаем плейлист
                loadPlaylist(currentPlaylistId)
            } catch (e: Exception) {
                _state.value = PlaylistState.Error("Не удалось удалить трек")
            }
        }
    }

    fun deletePlaylist() {
        viewModelScope.launch {
            try {
                playlistInteractor.deletePlaylist(currentPlaylistId)
                _deleteResult.value = DeleteResult.Success
            } catch (e: Exception) {
                _deleteResult.value = DeleteResult.Error(e.message ?: "Не удалось удалить плейлист")
            }
        }
    }

    fun clearDeleteResult() {
        _deleteResult.value = null
    }

    private fun updateState(playlist: Playlist, tracks: List<Track>) {
        val totalDurationMs = tracks.sumOf { it.trackTimeMillis }
        val totalDurationMinutes = (totalDurationMs / 60000).toInt()

        val tracksCountText = tracksCountPlurals?.invoke(tracks.size)
            ?: formatTracksCountFallback(tracks.size)
        val totalDurationText = minutesCountPlurals?.invoke(totalDurationMinutes)
            ?: formatDurationFallback(totalDurationMinutes)

        _state.value = PlaylistState.Content(
            playlist = playlist,
            tracks = tracks,
            totalDuration = totalDurationText,
            tracksCount = tracksCountText
        )
    }

    // Fallback методы на случай, если plurals не установлены
    private fun formatDurationFallback(minutes: Int): String {
        return when {
            minutes % 10 == 1 && minutes % 100 != 11 -> "$minutes минута"
            minutes % 10 in 2..4 && minutes % 100 !in 12..14 -> "$minutes минуты"
            else -> "$minutes минут"
        }
    }

    private fun formatTracksCountFallback(count: Int): String {
        return when {
            count % 10 == 1 && count % 100 != 11 -> "$count трек"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "$count трека"
            else -> "$count треков"
        }
    }
}

sealed class DeleteResult {
    object Success : DeleteResult()
    data class Error(val message: String) : DeleteResult()
}