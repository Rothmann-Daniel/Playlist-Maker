package com.example.playlistmaker.player.di

import com.example.playlistmaker.player.data.repository.AudioPlayerRepositoryImpl
import com.example.playlistmaker.player.domain.repository.AudioPlayerRepository
import com.example.playlistmaker.player.domain.usecase.*
import com.example.playlistmaker.player.ui.AudioPlayerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val playerModule = module {
    // Репозиторий
    single<AudioPlayerRepository> { AudioPlayerRepositoryImpl() }

    // UseCases
    factory { PrepareAudioUseCase(get()) }
    factory { StartAudioUseCase(get()) }
    factory { PauseAudioUseCase(get()) }
    factory { StopAudioUseCase(get()) }
    factory { IsAudioPlayingUseCase(get()) }
    factory { GetAudioPositionUseCase(get()) }
    factory { ReleasePlayerUseCase(get()) }
    factory { SetCompletionListenerUseCase(get()) }

    // ViewModel
    viewModel {
        AudioPlayerViewModel(
            prepareAudioUseCase = get(),
            startAudioUseCase = get(),
            pauseAudioUseCase = get(),
            stopAudioUseCase = get(),
            isAudioPlayingUseCase = get(),
            getAudioPositionUseCase = get(),
            releasePlayerUseCase = get(),
            setCompletionListenerUseCase = get()
        )
    }
}