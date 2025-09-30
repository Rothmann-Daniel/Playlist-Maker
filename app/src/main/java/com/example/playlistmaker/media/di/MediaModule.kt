package com.example.playlistmaker.media.di

import com.example.playlistmaker.media.data.repository.FavoriteTracksRepositoryImpl
import com.example.playlistmaker.media.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.media.domain.repository.FavoriteTracksRepository
import com.example.playlistmaker.media.ui.FavoriteTracksViewModel
import com.example.playlistmaker.media.ui.MediaViewModel
import com.example.playlistmaker.media.ui.PlaylistsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.scope.get
import org.koin.dsl.module


val mediaModule = module {

    // Repository
    single<FavoriteTracksRepository> {
        FavoriteTracksRepositoryImpl(get())
    }

    // Interactor
    factory {
        FavoriteTracksInteractor(get())
    }

    // ViewModels
    viewModel { MediaViewModel() }
    viewModel { PlaylistsViewModel() }
    viewModel {
        FavoriteTracksViewModel(get())
    }

}