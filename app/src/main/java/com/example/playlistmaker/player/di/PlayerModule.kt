package com.example.playlistmaker.player.di

import com.example.playlistmaker.player.data.repository.AndroidMediaPlayerProviderImpl
import com.example.playlistmaker.player.data.repository.AudioPlayerRepositoryImpl
import com.example.playlistmaker.player.domain.repository.AudioPlayerRepository
import com.example.playlistmaker.player.domain.repository.MediaPlayerProvider
import com.example.playlistmaker.player.domain.interactor.AudioPlayerInteractor
import com.example.playlistmaker.player.ui.AudioPlayerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val playerModule = module {
    // Провайдер MediaPlayer
    single<MediaPlayerProvider> { AndroidMediaPlayerProviderImpl() }

    // Репозиторий
    single<AudioPlayerRepository> { AudioPlayerRepositoryImpl(get()) }

    // Интерактор
    factory { AudioPlayerInteractor(get()) }

    // ViewModel
    viewModel {
        AudioPlayerViewModel(
            audioPlayerInteractor = get(),
            favoriteTracksInteractor = get(), // Добавляем интерактор избранного
            playlistInteractor = get()
        )
    }
}