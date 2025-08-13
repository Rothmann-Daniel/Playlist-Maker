package com.example.playlistmaker.media.di

import com.example.playlistmaker.media.ui.FavoriteTracksViewModel
import com.example.playlistmaker.media.ui.PlaylistsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val mediaModule = module {

    // ViewModels
    viewModelOf(::FavoriteTracksViewModel)
    viewModelOf(::PlaylistsViewModel)

}