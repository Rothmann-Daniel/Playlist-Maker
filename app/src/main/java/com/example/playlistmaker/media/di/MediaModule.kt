package com.example.playlistmaker.media.di

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.example.playlistmaker.media.ui.FavoriteTracksFragment
import com.example.playlistmaker.media.ui.FavoriteTracksViewModel
import com.example.playlistmaker.media.ui.MediaViewPagerAdapter
import com.example.playlistmaker.media.ui.PlaylistsFragment
import com.example.playlistmaker.media.ui.PlaylistsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val mediaModule = module {
    // Регистрируем адаптер
    factory { (fragmentManager: FragmentManager, lifecycle: Lifecycle) ->
        MediaViewPagerAdapter(fragmentManager, lifecycle)
    }

    // ViewModels
    viewModelOf(::FavoriteTracksViewModel)
    viewModelOf(::PlaylistsViewModel)

    // Fragments
    scope<FavoriteTracksFragment> {
        scoped { FavoriteTracksFragment.newInstance() }
    }

    scope<PlaylistsFragment> {
        scoped { PlaylistsFragment.newInstance() }
    }
}