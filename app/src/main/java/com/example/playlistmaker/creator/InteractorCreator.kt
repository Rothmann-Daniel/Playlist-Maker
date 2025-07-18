package com.example.playlistmaker.creator


import android.content.Context
import com.example.playlistmaker.player.data.repository.AudioPlayerRepositoryImpl
import com.example.playlistmaker.player.domain.repository.AudioPlayerRepository
import com.example.playlistmaker.player.domain.usecase.*
import com.example.playlistmaker.search.data.network.NetworkClient
import com.example.playlistmaker.search.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.search.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.search.domain.repository.TrackRepository
import com.example.playlistmaker.search.domain.usecase.*
import com.example.playlistmaker.settings.data.repository.SettingsRepositoryImpl
import com.example.playlistmaker.settings.domain.usecase.GetThemeSettingsUseCase
import com.example.playlistmaker.settings.domain.usecase.UpdateThemeSettingsUseCase
import com.example.playlistmaker.util.App

object InteractorCreator {
    // Search-related dependencies
    private val searchHistoryRepository by lazy {
        SearchHistoryRepositoryImpl(
            App.instance.getSharedPreferences("search_history_prefs", Context.MODE_PRIVATE)
        )
    }

    private val networkClient by lazy { NetworkClient.create() }

    private val trackRepository: TrackRepository by lazy {
        TrackRepositoryImpl(networkClient, searchHistoryRepository)
    }

    // Search UseCases
    val searchTracksUseCase by lazy { SearchTracksUseCase(trackRepository) }
    val addTrackToHistoryUseCase by lazy { AddTrackToHistoryUseCase(trackRepository) }
    val getSearchHistoryUseCase by lazy { GetSearchHistoryUseCase(trackRepository) }
    val clearSearchHistoryUseCase by lazy { ClearSearchHistoryUseCase(trackRepository) }

    // Player-related dependencies
    private val audioPlayerRepository: AudioPlayerRepository by lazy {
        AudioPlayerRepositoryImpl()
    }

    // Player UseCases
    val prepareAudioUseCase by lazy { PrepareAudioUseCase(audioPlayerRepository) }
    val startAudioUseCase by lazy { StartAudioUseCase(audioPlayerRepository) }
    val pauseAudioUseCase by lazy { PauseAudioUseCase(audioPlayerRepository) }
    val stopAudioUseCase by lazy { StopAudioUseCase(audioPlayerRepository) }
    val isAudioPlayingUseCase by lazy { IsAudioPlayingUseCase(audioPlayerRepository) }
    val getAudioPositionUseCase by lazy { GetAudioPositionUseCase(audioPlayerRepository) }
    val releasePlayerUseCase by lazy { ReleasePlayerUseCase(audioPlayerRepository) }
    val setCompletionListenerUseCase by lazy {
        SetCompletionListenerUseCase(audioPlayerRepository)
    }

    // Settings UseCases
    val getThemeSettingsUseCase by lazy {
        GetThemeSettingsUseCase(SettingsRepositoryImpl(App.instance.sharedPrefs))
    }
    val updateThemeSettingsUseCase by lazy {
        UpdateThemeSettingsUseCase(SettingsRepositoryImpl(App.instance.sharedPrefs))
    }
}