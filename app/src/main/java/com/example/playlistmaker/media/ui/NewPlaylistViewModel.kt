package com.example.playlistmaker.media.ui

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.media.domain.interactor.PlaylistInteractor
import kotlinx.coroutines.launch

/**
 * ViewModel для создания плейлиста
 * Упрощена - работа с файлами перенесена в репозиторий
 */
class NewPlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Состояния

    sealed interface CreatePlaylistState {
        object Idle : CreatePlaylistState
        object Loading : CreatePlaylistState
        data class Success(val playlistName: String) : CreatePlaylistState
        data class Error(val message: String) : CreatePlaylistState
    }

    private val _createState = MutableLiveData<CreatePlaylistState>(CreatePlaylistState.Idle)
    val createState: LiveData<CreatePlaylistState> = _createState

    private val _showExitDialog = MutableLiveData<Boolean>()
    val showExitDialog: LiveData<Boolean> = _showExitDialog

    private val _isCreateButtonEnabled = MutableLiveData<Boolean>()
    val isCreateButtonEnabled: LiveData<Boolean> = _isCreateButtonEnabled

    // Данные формы с сохранением состояния
    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    private val _description = MutableLiveData<String>()
    val description: LiveData<String> = _description

    private val _coverUri = MutableLiveData<Uri?>()
    val coverUri: LiveData<Uri?> = _coverUri

    private var hasUnsavedChanges = false

    init {
        // Восстанавливаем состояние
        restoreState()
    }

    private fun restoreState() {
        _name.value = savedStateHandle.get<String>(KEY_NAME) ?: ""
        _description.value = savedStateHandle.get<String>(KEY_DESCRIPTION) ?: ""
        _coverUri.value = savedStateHandle.get<String>(KEY_COVER_URI)?.let { Uri.parse(it) }

        updateCreateButtonState()
        updateUnsavedChanges()
    }

    fun onNameChanged(name: String) {
        _name.value = name
        savedStateHandle[KEY_NAME] = name
        updateCreateButtonState()
        updateUnsavedChanges()
    }

    fun onDescriptionChanged(description: String) {
        _description.value = description
        savedStateHandle[KEY_DESCRIPTION] = description
        updateUnsavedChanges()
    }

    fun setCoverUri(uri: Uri?) {
        _coverUri.value = uri
        savedStateHandle[KEY_COVER_URI] = uri?.toString()
        updateUnsavedChanges()
    }

    fun createPlaylist() {
        val currentName = _name.value?.trim()
        if (currentName.isNullOrBlank()) {
            _createState.value = CreatePlaylistState.Error("Название плейлиста не может быть пустым")
            return
        }

        _createState.value = CreatePlaylistState.Loading

        viewModelScope.launch {
            val result = playlistInteractor.createPlaylist(
                name = currentName,
                description = _description.value?.trim()?.takeIf { it.isNotEmpty() },
                coverImageUri = _coverUri.value
            )

            _createState.value = when {
                result.isSuccess -> {
                    hasUnsavedChanges = false
                    CreatePlaylistState.Success(currentName)
                }
                else -> CreatePlaylistState.Error(
                    result.exceptionOrNull()?.message ?: "Неизвестная ошибка"
                )
            }
        }
    }

    fun checkUnsavedChanges(): Boolean = hasUnsavedChanges

    fun showExitDialog() {
        _showExitDialog.value = true
    }

    fun hideExitDialog() {
        _showExitDialog.value = false
    }

    fun exitWithoutSaving() {
        _showExitDialog.value = false
        hasUnsavedChanges = false
        // Очищаем сохраненное состояние
        savedStateHandle.remove<String>(KEY_NAME)
        savedStateHandle.remove<String>(KEY_DESCRIPTION)
        savedStateHandle.remove<String>(KEY_COVER_URI)
    }

    private fun updateCreateButtonState() {
        _isCreateButtonEnabled.value = !_name.value.isNullOrBlank()
    }

    private fun updateUnsavedChanges() {
        val currentName = _name.value ?: ""
        val currentDescription = _description.value ?: ""
        val currentCoverUri = _coverUri.value

        hasUnsavedChanges = currentName.isNotBlank() ||
                currentDescription.isNotBlank() ||
                currentCoverUri != null
    }

    companion object {
        private const val KEY_NAME = "playlist_name"
        private const val KEY_DESCRIPTION = "playlist_description"
        private const val KEY_COVER_URI = "playlist_cover_uri"
    }
}