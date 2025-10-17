package com.example.playlistmaker.player.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.media.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.media.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.media.domain.model.Playlist
import com.example.playlistmaker.player.domain.interactor.AudioPlayerInteractor
import com.example.playlistmaker.player.domain.model.AudioPlayerState
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*

class AudioPlayerViewModel(
    private val audioPlayerInteractor: AudioPlayerInteractor,
    private val favoriteTracksInteractor: FavoriteTracksInteractor,
    private val playlistInteractor: PlaylistInteractor // Добавляем интерактор плейлистов
) : ViewModel() {

    sealed class PlayerState {
        object Preparing : PlayerState()
        object Prepared : PlayerState()
        object Playing : PlayerState()
        object Paused : PlayerState()
        data class Error(val message: String) : PlayerState()
    }

    private val _playerState = MutableLiveData<PlayerState>(PlayerState.Paused)
    val playerState: LiveData<PlayerState> = _playerState

    private val _currentPosition = MutableLiveData<String>("00:00")
    val currentPosition: LiveData<String> = _currentPosition

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    private var currentTrack: Track? = null

    fun setTrack(track: Track) {
        currentTrack = track
        // Проверяем актуальное состояние из БД
        checkFavoriteStatus(track)
    }

    private fun checkFavoriteStatus(track: Track) {
        viewModelScope.launch {
            favoriteTracksInteractor.getAllFavoriteTracks()
                .onEach { favoriteTracks ->
                    val isFav = favoriteTracks.any { it.trackId == track.trackId }
                    track.isFavorite = isFav
                    _isFavorite.postValue(isFav)
                }
                .catch { error ->
                    Log.e("AudioPlayer", "Error checking favorite status", error)
                    _isFavorite.postValue(false)
                }
                .launchIn(this)
        }
    }

    fun onFavoriteClicked() {
        val track = currentTrack ?: return

        viewModelScope.launch {
            if (track.isFavorite) {
                favoriteTracksInteractor.removeTrackFromFavorites(track)
            } else {
                favoriteTracksInteractor.addTrackToFavorites(track)
            }
            // Обновляем состояние
            track.isFavorite = !track.isFavorite
            _isFavorite.value = track.isFavorite
        }
    }

    private var clickDebounceJob: Job? = null

    fun preparePlayer(url: String) {
        audioPlayerInteractor.prepareAudio(url)
            .onEach { state ->
                _playerState.postValue(mapToUiState(state))
            }
            .catch { error ->
                _playerState.postValue(PlayerState.Error(error.message ?: "Unknown error"))
            }
            .launchIn(viewModelScope)

        audioPlayerInteractor.getPlaybackProgress()
            .onEach { position ->
                _currentPosition.postValue(formatTime(position))
            }
            .catch { error ->
                Log.e("AudioPlayer", "Playback progress error", error)
                _currentPosition.postValue(formatTime(0L))
            }
            .launchIn(viewModelScope)

        audioPlayerInteractor.getPlayerState()
            .onEach { state ->
                _playerState.postValue(mapToUiState(state))
                when (state) {
                    is AudioPlayerState.Completed -> {
                        _currentPosition.postValue("00:00")
                    }
                    is AudioPlayerState.Paused,
                    is AudioPlayerState.Prepared -> {
                        // При паузе или подготовке не сбрасываем позицию
                    }
                    else -> {}
                }
            }
            .launchIn(viewModelScope)
    }

    fun togglePlayPause() {
        clickDebounceJob?.cancel()
        clickDebounceJob = viewModelScope.launch {
            delay(CLICK_DEBOUNCE_DELAY_MS)

            when (playerState.value) {
                is PlayerState.Playing -> pause()
                is PlayerState.Paused -> play()
                is PlayerState.Prepared -> play()
                else -> {}
            }
        }
    }

    private fun play() {
        viewModelScope.launch {
            audioPlayerInteractor.startAudio()
            _playerState.postValue(PlayerState.Playing)
        }
    }

    fun pause() {
        viewModelScope.launch {
            audioPlayerInteractor.pauseAudio()
            _playerState.postValue(PlayerState.Paused)
        }
    }

    fun stop() {
        viewModelScope.launch {
            audioPlayerInteractor.stopAudio()
            _playerState.postValue(PlayerState.Paused)
            _currentPosition.postValue("00:00")
        }
    }

    private fun mapToUiState(state: AudioPlayerState): PlayerState {
        return when (state) {
            is AudioPlayerState.Preparing -> PlayerState.Preparing
            is AudioPlayerState.Prepared -> PlayerState.Prepared
            is AudioPlayerState.Playing -> PlayerState.Playing
            is AudioPlayerState.Paused -> PlayerState.Paused
            is AudioPlayerState.Error -> PlayerState.Error(state.message)
            else -> PlayerState.Paused
        }
    }


    // Работа с PlayList

    private val _playlistsState = MutableLiveData<PlaylistsState>()
    val playlistsState: LiveData<PlaylistsState> = _playlistsState

    private val _addToPlaylistResult = MutableLiveData<AddToPlaylistResult?>()
    val addToPlaylistResult: LiveData<AddToPlaylistResult?> = _addToPlaylistResult

    sealed class PlaylistsState {
        object Loading : PlaylistsState()
        object Empty : PlaylistsState()
        data class Content(val playlists: List<Playlist>) : PlaylistsState()
        data class Error(val message: String) : PlaylistsState()
    }

    sealed class AddToPlaylistResult {
        data class Success(val playlistName: String) : AddToPlaylistResult()
        data class AlreadyExists(val playlistName: String) : AddToPlaylistResult()
        data class Error(val message: String) : AddToPlaylistResult()
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            _playlistsState.value = PlaylistsState.Loading
            try {
                playlistInteractor.getAllPlaylists().collect { playlists ->
                    _playlistsState.value = if (playlists.isEmpty()) {
                        PlaylistsState.Empty
                    } else {
                        PlaylistsState.Content(playlists)
                    }
                }
            } catch (e: Exception) {
                _playlistsState.value = PlaylistsState.Error("Failed to load playlists")
            }
        }
    }


    fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        viewModelScope.launch {
            try {
                // Проверяем, есть ли уже трек в плейлисте
                val isTrackInPlaylist = playlistInteractor.isTrackInPlaylist(playlist.playlistId, track.trackId)

                if (isTrackInPlaylist) {
                    _addToPlaylistResult.value = AddToPlaylistResult.AlreadyExists(playlist.name)
                    return@launch
                }

                // Добавляем трек в плейлист
                playlistInteractor.addTrackToPlaylist(playlist.playlistId, track)
                _addToPlaylistResult.value = AddToPlaylistResult.Success(playlist.name)

            } catch (e: Exception) {
                _addToPlaylistResult.value = AddToPlaylistResult.Error("Failed to add track to playlist: ${e.message}")
            }
        }
    }

    fun clearAddToPlaylistResult() {
        _addToPlaylistResult.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            audioPlayerInteractor.release()
        }
        clickDebounceJob?.cancel()
    }

    fun formatTime(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY_MS = 300L
    }
}