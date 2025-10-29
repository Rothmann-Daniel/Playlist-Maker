package com.example.playlistmaker.media.ui.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.media.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.media.domain.model.Playlist
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    sealed class PlaylistsState {
        object Empty : PlaylistsState()
        data class Content(val playlists: List<Playlist>) : PlaylistsState()
    }

    private val _state = MutableLiveData<PlaylistsState>()
    val state: LiveData<PlaylistsState> = _state

    init {
        loadPlaylists()
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            playlistInteractor.getAllPlaylists().collectLatest { playlists ->
                _state.value = if (playlists.isEmpty()) {
                    PlaylistsState.Empty
                } else {
                    PlaylistsState.Content(playlists)
                }
            }
        }
    }

    fun refresh() {
        loadPlaylists()
    }
}