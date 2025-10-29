package com.example.playlistmaker.media.ui.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.media.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.search.domain.model.Track
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class FavoriteTracksViewModel(
    private val interactor: FavoriteTracksInteractor
) : ViewModel() {

    sealed class FavoriteTracksState {
        object Empty : FavoriteTracksState()
        data class Content(val tracks: List<Track>) : FavoriteTracksState()
    }

    private val _state = MutableLiveData<FavoriteTracksState>()
    val state: LiveData<FavoriteTracksState> = _state

    init {
        loadFavoriteTracks()
    }

    private fun loadFavoriteTracks() {
        interactor.getAllFavoriteTracks()
            .onEach { tracks ->
                _state.value = if (tracks.isEmpty()) {
                    FavoriteTracksState.Empty
                } else {
                    FavoriteTracksState.Content(tracks)
                }
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        loadFavoriteTracks()
    }
}