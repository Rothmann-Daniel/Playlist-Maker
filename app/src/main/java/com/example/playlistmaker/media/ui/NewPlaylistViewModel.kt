package com.example.playlistmaker.media.ui

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.media.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.media.domain.model.Playlist
import com.google.gson.Gson
import kotlinx.coroutines.launch


class NewPlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor,
    private val fileManager: PlaylistFileManager,
    private val gson: Gson
) : ViewModel() {

    private val _playlistCreated = MutableLiveData<Boolean>()
    val playlistCreated: LiveData<Boolean> = _playlistCreated

    private val _showExitDialog = MutableLiveData<Boolean>()
    val showExitDialog: LiveData<Boolean> = _showExitDialog

    private val _coverUri = MutableLiveData<Uri?>()
    val coverUri: LiveData<Uri?> = _coverUri

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    private val _description = MutableLiveData<String>()
    val description: LiveData<String> = _description

    private val _isCreateButtonEnabled = MutableLiveData<Boolean>()
    val isCreateButtonEnabled: LiveData<Boolean> = _isCreateButtonEnabled

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var hasUnsavedChanges = false

    init {
        _name.value = ""
        _description.value = ""
        _isCreateButtonEnabled.value = false
        _coverUri.value = null
        _isLoading.value = false
    }

    fun onNameChanged(name: String) {
        _name.value = name
        _isCreateButtonEnabled.value = name.isNotBlank()
        updateUnsavedChanges()
    }

    fun onDescriptionChanged(description: String) {
        _description.value = description
        updateUnsavedChanges()
    }

    fun setCoverUri(uri: Uri?) {
        _coverUri.value = uri
        updateUnsavedChanges()
    }

    fun createPlaylist() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Сохраняем обложку, если она была выбрана
                val coverUriValue = _coverUri.value
                var finalCoverPath: String? = null

                if (coverUriValue != null) {
                    val inputStream = fileManager.getInputStreamFromUri(coverUriValue)
                    inputStream?.use { stream ->
                        finalCoverPath = fileManager.saveCoverImage(stream)
                    }
                }

                val playlist = Playlist(
                    name = _name.value ?: "",
                    description = _description.value?.takeIf { it.isNotBlank() },
                    coverImagePath = finalCoverPath,
                    trackIds = emptyList(),
                    tracksCount = 0
                )

                val playlistId = playlistInteractor.createPlaylist(playlist)
                if (playlistId > 0) {
                    _playlistCreated.value = true
                    hasUnsavedChanges = false
                } else {
                    _playlistCreated.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _playlistCreated.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkUnsavedChanges(): Boolean {
        return hasUnsavedChanges
    }

    fun showExitDialog() {
        _showExitDialog.value = true
    }

    fun hideExitDialog() {
        _showExitDialog.value = false
    }

    fun exitWithoutSaving() {
        _showExitDialog.value = false
        hasUnsavedChanges = false
    }

    private fun updateUnsavedChanges() {
        val currentName = _name.value ?: ""
        val currentDescription = _description.value ?: ""
        val currentCoverUri = _coverUri.value

        hasUnsavedChanges = currentName.isNotBlank() ||
                currentDescription.isNotBlank() ||
                currentCoverUri != null
    }
}