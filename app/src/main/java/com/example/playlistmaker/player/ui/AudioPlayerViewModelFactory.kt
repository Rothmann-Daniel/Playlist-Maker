package com.example.playlistmaker.player.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.player.domain.usecase.*

class AudioPlayerViewModelFactory(
    private val prepareAudioUseCase: PrepareAudioUseCase,
    private val startAudioUseCase: StartAudioUseCase,
    private val pauseAudioUseCase: PauseAudioUseCase,
    private val stopAudioUseCase: StopAudioUseCase,
    private val isAudioPlayingUseCase: IsAudioPlayingUseCase,
    private val getAudioPositionUseCase: GetAudioPositionUseCase,
    private val releasePlayerUseCase: ReleasePlayerUseCase,
    private val setCompletionListenerUseCase: SetCompletionListenerUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AudioPlayerViewModel(
            prepareAudioUseCase,
            startAudioUseCase,
            pauseAudioUseCase,
            stopAudioUseCase,
            isAudioPlayingUseCase,
            getAudioPositionUseCase,
            releasePlayerUseCase,
            setCompletionListenerUseCase
        ) as T
    }
}