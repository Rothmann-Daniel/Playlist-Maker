package com.example.playlistmaker.search.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.search.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.search.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.search.data.network.NetworkClient
import com.example.playlistmaker.search.domain.usecase.AddTrackToHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.ClearSearchHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.GetSearchHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.SearchTracksUseCase

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                val sharedPreferences = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
                val searchHistoryRepository = SearchHistoryRepositoryImpl(sharedPreferences)
                val networkClient = NetworkClient.create()
                val trackRepository = TrackRepositoryImpl(networkClient, searchHistoryRepository)

                SearchViewModel(
                    SearchTracksUseCase(trackRepository),
                    AddTrackToHistoryUseCase(trackRepository),
                    GetSearchHistoryUseCase(trackRepository),
                    ClearSearchHistoryUseCase(trackRepository)
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}