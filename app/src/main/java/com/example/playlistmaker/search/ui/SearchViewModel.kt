package com.example.playlistmaker.search.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.search.domain.model.NetworkResult
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.domain.usecase.AddTrackToHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.ClearSearchHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.GetSearchHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.SearchTracksUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchTracksUseCase: SearchTracksUseCase,
    private val addToHistoryUseCase: AddTrackToHistoryUseCase,
    private val getHistoryUseCase: GetSearchHistoryUseCase,
    private val clearHistoryUseCase: ClearSearchHistoryUseCase
) : ViewModel() {

    sealed interface SearchState {
        data object Loading : SearchState
        data class Content(val tracks: List<Track>) : SearchState
        data class Error(val message: String) : SearchState
        data class History(val tracks: List<Track>) : SearchState
        data object EmptyResult : SearchState
    }

    private val _state = MutableLiveData<SearchState>()
    val state: LiveData<SearchState> = _state

    private var searchJob: Job? = null
    private var lastSearchQuery: String? = null

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            try {
                val history = getHistoryUseCase()
                if (history.isNotEmpty()) {
                    _state.postValue(SearchState.History(history))
                } else {
                    _state.postValue(SearchState.Content(emptyList()))
                }
            } catch (e: Exception) {
                _state.postValue(SearchState.Content(emptyList()))
            }
        }
    }

    fun searchDebounced(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            performSearch(query)
        }
    }

    fun searchImmediately(query: String) {
        searchJob?.cancel()
        performSearch(query)
    }

    private fun performSearch(query: String) {
        lastSearchQuery = query
        _state.value = SearchState.Loading

        viewModelScope.launch {
            try {
                when (val result = searchTracksUseCase(query)) {
                    is NetworkResult.Success -> {
                        if (result.data.isEmpty()) {
                            _state.postValue(SearchState.EmptyResult)
                        } else {
                            _state.postValue(SearchState.Content(result.data))
                        }
                    }
                    is NetworkResult.Failure -> {
                        _state.postValue(SearchState.Error(result.error))
                    }
                }
            } catch (e: Exception) {
                _state.postValue(SearchState.Error(e.message ?: "Unknown error"))
            }
        }
    }

    fun updateHistoryVisibility(hasQuery: Boolean) {
        if (!hasQuery) {
            loadHistory()
        }
    }

    fun addTrackToHistory(track: Track) {
        viewModelScope.launch {
            try {
                addToHistoryUseCase(track)
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error adding to history", e)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            try {
                clearHistoryUseCase()
                _state.postValue(SearchState.Content(emptyList()))
            } catch (e: Exception) {
                _state.postValue(SearchState.Error("Failed to clear history"))
            }
        }
    }

    fun retryLastSearch() {
        lastSearchQuery?.let { performSearch(it) }
    }


    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}