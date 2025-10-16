package com.example.playlistmaker.media.di

import androidx.lifecycle.SavedStateHandle
import com.example.playlistmaker.media.data.repository.FavoriteTracksRepositoryImpl
import com.example.playlistmaker.media.data.repository.PlaylistRepositoryImpl
import com.example.playlistmaker.media.data.storage.PlaylistFileStorage
import com.example.playlistmaker.media.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.media.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.media.domain.repository.FavoriteTracksRepository
import com.example.playlistmaker.media.domain.repository.PlaylistRepository
import com.example.playlistmaker.media.ui.FavoriteTracksViewModel
import com.example.playlistmaker.media.ui.MediaViewModel
import com.example.playlistmaker.media.ui.NewPlaylistViewModel
import com.example.playlistmaker.media.ui.PlaylistsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val mediaModule = module {

    // File Storage
    single { PlaylistFileStorage(get()) }

    // Repositories
    single<FavoriteTracksRepository> {
        FavoriteTracksRepositoryImpl(get())
    }

    single<PlaylistRepository> {
        PlaylistRepositoryImpl(get(), get()) // database + fileStorage
    }

    // Interactors
    factory {
        FavoriteTracksInteractor(get())
    }

    factory {
        PlaylistInteractor(get())
    }

    // ViewModels
    viewModel { MediaViewModel() }

    viewModel { PlaylistsViewModel(get()) }

    viewModel { FavoriteTracksViewModel(get()) }

    viewModel { (savedStateHandle: SavedStateHandle) ->
        NewPlaylistViewModel(
            playlistInteractor = get(),
            savedStateHandle = savedStateHandle
        )
    }
}