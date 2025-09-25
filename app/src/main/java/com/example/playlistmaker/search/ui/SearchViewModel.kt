package com.example.playlistmaker.search.ui

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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchTracksUseCase: SearchTracksUseCase,
    private val addToHistoryUseCase: AddTrackToHistoryUseCase,
    private val getHistoryUseCase: GetSearchHistoryUseCase,
    private val clearHistoryUseCase: ClearSearchHistoryUseCase
) : ViewModel() {

    sealed interface SearchState {
        object Loading : SearchState
        data class Content(val tracks: List<Track>) : SearchState
        data class Error(val message: String) : SearchState
        data class History(val tracks: List<Track>) : SearchState
        object EmptyResult : SearchState
    }

    private val _state = MutableLiveData<SearchState>()
    val state: LiveData<SearchState> = _state

    private var searchJob: Job? = null
    private var lastSearchQuery: String? = null
    private var clickJob: Job? = null

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            getHistoryUseCase().collectLatest { history ->
                if (history.isNotEmpty()) {
                    _state.value = SearchState.History(history)
                } else {
                    _state.value = SearchState.Content(emptyList())
                }
            }
        }
    }

    fun searchDebounced(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            if (isActive) { // Проверка активности корутины
                performSearch(query)
            }
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
            searchTracksUseCase(query).collectLatest { result ->
                when (result) {
                    is NetworkResult.Loading -> _state.value = SearchState.Loading
                    is NetworkResult.Success -> {
                        if (result.data.isEmpty()) {
                            _state.value = SearchState.EmptyResult
                        } else {
                            _state.value = SearchState.Content(result.data)
                        }
                    }
                    is NetworkResult.Failure -> {
                        _state.value = SearchState.Error(result.error)
                    }
                }
            }
        }
    }

    fun onTrackClick(track: Track) {
        clickJob?.cancel()
        clickJob = viewModelScope.launch {
            addTrackToHistory(track)
            // Здесь будет навигация к аудиоплееру
            delay(CLICK_DEBOUNCE_DELAY)
        }
    }

    private suspend fun addTrackToHistory(track: Track) {
        try {
            addToHistoryUseCase(track)
            if (_state.value is SearchState.History) {
                loadHistory()
            }
        } catch (e: Exception) {
            // Логирование ошибки
        }
    }

    fun updateHistoryVisibility(hasQuery: Boolean) {
        if (!hasQuery) {
            loadHistory()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            try {
                clearHistoryUseCase()
                _state.value = SearchState.Content(emptyList())
            } catch (e: Exception) {
                _state.value = SearchState.Error("Failed to clear history")
            }
        }
    }

    fun retryLastSearch() {
        lastSearchQuery?.let { performSearch(it) }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}