package com.example.playlistmaker


import com.example.playlistmaker.data.network.NetworkClient
import com.example.playlistmaker.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.domain.repository.TrackRepository
import com.example.playlistmaker.domain.usecase.AddTrackToHistoryUseCase
import com.example.playlistmaker.domain.usecase.ClearSearchHistoryUseCase
import com.example.playlistmaker.domain.usecase.GetSearchHistoryUseCase
import com.example.playlistmaker.domain.usecase.SearchTracksUseCase

object InteractorCreator {

        private val networkClient by lazy {
            NetworkClient.create()
        }

        // Создаем реализацию репозитория
        private val trackRepository: TrackRepository by lazy {
            TrackRepositoryImpl(networkClient)
        }
        // Публичные UseCases для Activity
        val searchTracksUseCase by lazy {
            SearchTracksUseCase(trackRepository)
        }



    // Создаем интеракторы (UseCases)
    val searchTracks: SearchTracksUseCase by lazy {
        SearchTracksUseCase(trackRepository)
    }

    val addTrackToHistory: AddTrackToHistoryUseCase by lazy {
        AddTrackToHistoryUseCase(trackRepository)
    }

    val getSearchHistory: GetSearchHistoryUseCase by lazy {
        GetSearchHistoryUseCase(trackRepository)
    }

    val clearSearchHistory: ClearSearchHistoryUseCase by lazy {
        ClearSearchHistoryUseCase(trackRepository)
    }
}