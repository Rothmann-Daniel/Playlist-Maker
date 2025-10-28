package com.example.playlistmaker.media.ui.newplaylist

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.media.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.util.SingleLiveEvent
import kotlinx.coroutines.launch

open class NewPlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    sealed interface CreatePlaylistState {
        object Idle : CreatePlaylistState
        object Loading : CreatePlaylistState
        data class Success(val playlistName: String) : CreatePlaylistState
        data class Error(val messageResId: Int) : CreatePlaylistState
    }

    // Используем SingleLiveEvent для одноразовых событий
    protected val _createState = SingleLiveEvent<CreatePlaylistState>()
    val createState: LiveData<CreatePlaylistState> = _createState

    protected val _showExitDialog = SingleLiveEvent<Boolean>()
    val showExitDialog: LiveData<Boolean> = _showExitDialog

    protected val _isCreateButtonEnabled = MutableLiveData<Boolean>(false)
    val isCreateButtonEnabled: LiveData<Boolean> = _isCreateButtonEnabled

    protected val _name = MutableLiveData<String>("")
    val name: LiveData<String> = _name

    protected val _description = MutableLiveData<String>("")
    val description: LiveData<String> = _description

    protected val _coverUri = MutableLiveData<Uri?>(null)
    val coverUri: LiveData<Uri?> = _coverUri

    protected open var hasUnsavedChanges = false

    init {
        restoreState()
    }

    private fun restoreState() {
        val savedName = savedStateHandle.get<String>(KEY_NAME) ?: ""
        val savedDescription = savedStateHandle.get<String>(KEY_DESCRIPTION) ?: ""
        val savedCoverUri = savedStateHandle.get<String>(KEY_COVER_URI)?.let { Uri.parse(it) }

        _name.value = savedName
        _description.value = savedDescription
        _coverUri.value = savedCoverUri

        updateUnsavedChanges()
        updateCreateButtonState()
    }

    open fun onNameChanged(name: String) {
        _name.value = name
        savedStateHandle[KEY_NAME] = name
        updateCreateButtonState()
        updateUnsavedChanges()
    }

    open fun onDescriptionChanged(description: String) {
        _description.value = description
        savedStateHandle[KEY_DESCRIPTION] = description
        updateUnsavedChanges()
    }

    open fun setCoverUri(uri: Uri?) {
        _coverUri.value = uri
        savedStateHandle[KEY_COVER_URI] = uri?.toString()
        updateUnsavedChanges()
    }

    open fun createPlaylist() {
        val currentName = _name.value?.trim()
        if (currentName.isNullOrBlank()) {
            _createState.value = CreatePlaylistState.Error(
                com.example.playlistmaker.R.string.error_empty_playlist_name
            )
            return
        }

        _createState.value = CreatePlaylistState.Loading

        viewModelScope.launch {
            try {
                val result = playlistInteractor.createPlaylist(
                    name = currentName,
                    description = _description.value?.trim()?.takeIf { it.isNotEmpty() },
                    coverImageUri = _coverUri.value
                )

                if (result.isSuccess) {
                    hasUnsavedChanges = false
                    clearSavedState()
                    _createState.value = CreatePlaylistState.Success(currentName)
                } else {
                    _createState.value = CreatePlaylistState.Error(
                        com.example.playlistmaker.R.string.error_unknown
                    )
                }
            } catch (e: Exception) {
                _createState.value = CreatePlaylistState.Error(
                    com.example.playlistmaker.R.string.error_unknown
                )
            }
        }
    }

    open fun checkUnsavedChanges(): Boolean {
        return hasUnsavedChanges
    }

    open fun showExitDialog() {
        _showExitDialog.value = true
    }

    open fun hideExitDialog() {
        _showExitDialog.value = false
    }

    open fun exitWithoutSaving() {
        _showExitDialog.value = false
        hasUnsavedChanges = false
        clearSavedState()
    }

    protected open fun updateCreateButtonState() {
        val currentName = _name.value
        val isEnabled = !currentName.isNullOrBlank()
        _isCreateButtonEnabled.value = isEnabled
    }

    protected open fun updateUnsavedChanges() {
        val currentName = _name.value ?: ""
        val currentDescription = _description.value ?: ""
        val currentCoverUri = _coverUri.value

        val newHasUnsavedChanges = currentName.isNotBlank() ||
                currentDescription.isNotBlank() ||
                currentCoverUri != null

        if (newHasUnsavedChanges != hasUnsavedChanges) {
            hasUnsavedChanges = newHasUnsavedChanges
        }
    }

    protected fun clearSavedState() {
        savedStateHandle.remove<String>(KEY_NAME)
        savedStateHandle.remove<String>(KEY_DESCRIPTION)
        savedStateHandle.remove<String>(KEY_COVER_URI)
    }

    companion object {
        private const val KEY_NAME = "playlist_name"
        private const val KEY_DESCRIPTION = "playlist_description"
        private const val KEY_COVER_URI = "playlist_cover_uri"
    }
}