package com.example.playlistmaker.media.di

import com.example.playlistmaker.media.data.repository.FavoriteTracksRepositoryImpl
import com.example.playlistmaker.media.data.repository.PlaylistRepositoryImpl
import com.example.playlistmaker.media.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.media.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.media.domain.repository.FavoriteTracksRepository
import com.example.playlistmaker.media.domain.repository.PlaylistRepository
import com.example.playlistmaker.media.ui.FavoriteTracksViewModel
import com.example.playlistmaker.media.ui.MediaViewModel
import com.example.playlistmaker.media.ui.NewPlaylistViewModel
import com.example.playlistmaker.media.ui.PlaylistFileManager
import com.example.playlistmaker.media.ui.PlaylistsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val mediaModule = module {

    // Repository
    single<FavoriteTracksRepository> {
        FavoriteTracksRepositoryImpl(get())
    }

    single<PlaylistRepository> {
        PlaylistRepositoryImpl(get())
    }

    // Interactor
    factory {
        FavoriteTracksInteractor(get())
    }

    factory {
        PlaylistInteractor(get())
    }

    // File Manager
    factory {
        PlaylistFileManager(androidContext())
    }

    // ViewModels
    viewModel { MediaViewModel() }
    viewModel { PlaylistsViewModel(get()) }
    viewModel {
        FavoriteTracksViewModel(get())
    }
    viewModel {
        NewPlaylistViewModel(
            playlistInteractor = get(),
            fileManager = get(),
            gson = get()
        )
    }
}