package com.example.playlistmaker.creator


import android.content.Context
import com.example.playlistmaker.search.data.network.NetworkClient
import com.example.playlistmaker.search.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.search.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.search.domain.repository.TrackRepository
import com.example.playlistmaker.search.domain.usecase.AddTrackToHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.ClearSearchHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.GetSearchHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.SearchTracksUseCase
import com.example.playlistmaker.util.App



object InteractorCreator {
    private val searchHistoryRepository by lazy {
        SearchHistoryRepositoryImpl(
            App.instance.getSharedPreferences("search_history_prefs", Context.MODE_PRIVATE)
        )
    }

    private val networkClient by lazy { NetworkClient.create() }

    private val trackRepository: TrackRepository by lazy {
        TrackRepositoryImpl(networkClient, searchHistoryRepository)
    }

    // UseCases
    val searchTracksUseCase by lazy { SearchTracksUseCase(trackRepository) }
    val addTrackToHistoryUseCase by lazy { AddTrackToHistoryUseCase(trackRepository) }
    val getSearchHistoryUseCase by lazy { GetSearchHistoryUseCase(trackRepository) }
    val clearSearchHistoryUseCase by lazy { ClearSearchHistoryUseCase(trackRepository) }
}