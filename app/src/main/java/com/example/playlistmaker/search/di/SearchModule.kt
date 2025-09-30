package com.example.playlistmaker.search.di


import com.example.playlistmaker.search.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.search.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.search.domain.repository.SearchHistoryRepository
import com.example.playlistmaker.search.domain.repository.TrackRepository
import com.example.playlistmaker.search.domain.usecase.AddTrackToHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.ClearSearchHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.GetSearchHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.SearchTracksUseCase
import com.example.playlistmaker.search.ui.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val searchModule = module {
    single<TrackRepository> {
        TrackRepositoryImpl(
            networkClient = get(),
            searchHistoryRepository = get(),
            database = get() // Добавляем базу данных
        )
    }

    single<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(
            sharedPreferences = get(named("search_history_prefs")),
            gson = get()
        )
    }

    factory { SearchTracksUseCase(get()) }
    factory { AddTrackToHistoryUseCase(get()) }
    factory { GetSearchHistoryUseCase(get()) }
    factory { ClearSearchHistoryUseCase(get()) }

    viewModel {
        SearchViewModel(
            searchTracksUseCase = get(),
            addToHistoryUseCase = get(),
            getHistoryUseCase = get(),
            clearHistoryUseCase = get()
        )
    }
}