package com.example.playlistmaker.search.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.creator.InteractorCreator


class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(
                    // берем из InteractorCreator
                    InteractorCreator.searchTracksUseCase,
                    InteractorCreator.addTrackToHistoryUseCase,
                    InteractorCreator.getSearchHistoryUseCase,
                    InteractorCreator.clearSearchHistoryUseCase
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}