package com.example.playlistmaker.media.di

import androidx.lifecycle.SavedStateHandle
import com.example.playlistmaker.media.data.db.AppDatabase
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

    // DAOs
    single { get<AppDatabase>().playlistDao() }
    single { get<AppDatabase>().playlistTrackDao() }
    single { get<AppDatabase>().playlistTrackDataDao() }
    single { get<AppDatabase>().trackDao() }

    // Repositories
    single<FavoriteTracksRepository> {
        FavoriteTracksRepositoryImpl(get()) // передаем TrackDao
    }

    single<PlaylistRepository> {
        PlaylistRepositoryImpl(
            playlistDao = get(),
            playlistTrackDao = get(),
            playlistTrackDataDao = get(),
            favoriteTrackDao = get(),
            fileStorage = get()
        )
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