package com.example.playlistmaker.media.ui.openplaylist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.media.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.media.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.launch

class OpenPlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    sealed class PlaylistState {
        object Loading : PlaylistState()
        data class Content(
            val playlist: Playlist,
            val tracks: List<Track>,
            val totalDuration: String,
            val tracksCount: String
        ) : PlaylistState()
        data class Error(val message: String) : PlaylistState()
    }

    private val _state = MutableLiveData<PlaylistState>()
    val state: LiveData<PlaylistState> = _state

    // Эти поля должны быть инициализированы из Activity/Fragment с контекстом
    var tracksCountPlurals: ((Int) -> String)? = null
    var minutesCountPlurals: ((Int) -> String)? = null

    fun loadPlaylist(playlistId: Long) {
        _state.value = PlaylistState.Loading

        viewModelScope.launch {
            try {
                val playlist = playlistInteractor.getPlaylistById(playlistId)

                if (playlist == null) {
                    _state.value = PlaylistState.Error("Плейлист не найден")
                    return@launch
                }

                val tracks = playlistInteractor.getTracksForPlaylist(playlistId)

                // Вычисляем общую длительность в минутах
                val totalDurationMs = tracks.sumOf { it.trackTimeMillis }
                val totalDurationMinutes = (totalDurationMs / 60000).toInt()

                // Форматируем количество треков и минут с использованием plurals
                val tracksCountText = tracksCountPlurals?.invoke(tracks.size) ?: formatTracksCountFallback(tracks.size)
                val totalDurationText = minutesCountPlurals?.invoke(totalDurationMinutes) ?: formatDurationFallback(totalDurationMinutes)

                _state.value = PlaylistState.Content(
                    playlist = playlist,
                    tracks = tracks,
                    totalDuration = totalDurationText,
                    tracksCount = tracksCountText
                )
            } catch (e: Exception) {
                _state.value = PlaylistState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    // Фолбэк методы на случай, если plurals не были установлены
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