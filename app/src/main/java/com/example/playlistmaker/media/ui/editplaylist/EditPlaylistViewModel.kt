package com.example.playlistmaker.media.ui.editplaylist

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.media.data.storage.PlaylistFileStorage
import com.example.playlistmaker.media.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.media.domain.model.Playlist
import com.example.playlistmaker.media.ui.newplaylist.NewPlaylistViewModel
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel для редактирования плейлиста
 * Наследуется от NewPlaylistViewModel и переопределяет логику
 */
class EditPlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor,
    private val fileStorage: PlaylistFileStorage,
    savedStateHandle: SavedStateHandle,
    private val playlistId: Long
) : NewPlaylistViewModel(playlistInteractor, savedStateHandle) {

    private val _playlist = MutableLiveData<Playlist?>()
    val playlist: LiveData<Playlist?> = _playlist

    private var originalCoverPath: String? = null
    private var isInitialLoad = true
    private var originalName: String = ""
    private var originalDescription: String = ""
    private var originalCoverUri: Uri? = null

    init {
        loadPlaylistData()
    }

    /**
     * Загружает данные плейлиста для редактирования
     */
    private fun loadPlaylistData() {
        viewModelScope.launch {
            try {
                val playlist = playlistInteractor.getPlaylistById(playlistId)
                _playlist.value = playlist

                playlist?.let {
                    // Сохраняем оригинальные данные
                    originalName = it.name
                    originalDescription = it.description ?: ""
                    originalCoverPath = it.coverImagePath

                    // Временно отключаем observer для предотвращения двойного вызова
                    isInitialLoad = true

                    // Заполняем поля формы через родительские методы
                    onNameChanged(it.name)
                    onDescriptionChanged(it.description ?: "")

                    // Устанавливаем URI обложки, если есть
                    it.coverImagePath?.let { path ->
                        val file = File(path)
                        if (file.exists()) {
                            val uri = Uri.fromFile(file)
                            originalCoverUri = uri
                            setCoverUri(uri)
                        }
                    }

                    // Включаем observer обратно
                    isInitialLoad = false

                    // Обновляем состояние кнопки после загрузки данных
                    updateCreateButtonState()
                    updateUnsavedChanges()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _createState.value = CreatePlaylistState.Error("Не удалось загрузить данные плейлиста")
            }
        }
    }

    /**
     * Переопределяем методы изменения данных для правильного отслеживания изменений
     */
    override fun onNameChanged(name: String) {
        super.onNameChanged(name)
        if (!isInitialLoad) {
            updateCreateButtonState()
            updateUnsavedChanges()
        }
    }

    override fun onDescriptionChanged(description: String) {
        super.onDescriptionChanged(description)
        if (!isInitialLoad) {
            updateUnsavedChanges()
        }
    }

    override fun setCoverUri(uri: Uri?) {
        super.setCoverUri(uri)
        if (!isInitialLoad) {
            updateUnsavedChanges()
        }
    }

    /**
     * Обновляет плейлист вместо создания нового
     */
    fun updatePlaylist() {
        val currentName = _name.value?.trim()
        if (currentName.isNullOrBlank()) {
            _createState.value = CreatePlaylistState.Error("Название плейлиста не может быть пустым")
            return
        }

        _createState.value = CreatePlaylistState.Loading

        viewModelScope.launch {
            try {
                val currentPlaylist = _playlist.value ?: run {
                    _createState.value = CreatePlaylistState.Error("Плейлист не найден")
                    return@launch
                }

                // Определяем путь к обложке
                val coverPath = when {
                    // Если выбрана новая обложка (URI изменился)
                    coverUri.value != null && hasCoverChanged() -> {
                        coverUri.value?.let { uri ->
                            fileStorage.saveCoverImageFromUri(uri)
                        }
                    }
                    // Если обложка не изменилась
                    else -> originalCoverPath
                }

                // Создаем обновленный плейлист
                val updatedPlaylist = currentPlaylist.copy(
                    name = currentName,
                    description = _description.value?.trim()?.takeIf { it.isNotEmpty() },
                    coverImagePath = coverPath
                )

                // Обновляем плейлист
                val result = playlistInteractor.updatePlaylist(updatedPlaylist)

                if (result.isSuccess) {
                    // Удаляем старую обложку, если была заменена
                    if (coverPath != originalCoverPath && originalCoverPath != null) {
                        fileStorage.deleteCoverImage(originalCoverPath)
                    }
                    hasUnsavedChanges = false
                    clearSavedState()
                    _createState.value = CreatePlaylistState.Success(currentName)
                } else {
                    _createState.value = CreatePlaylistState.Error(
                        result.exceptionOrNull()?.message ?: "Неизвестная ошибка"
                    )
                }
            } catch (e: Exception) {
                _createState.value = CreatePlaylistState.Error(e.message ?: "Ошибка обновления")
            }
        }
    }

    /**
     * Проверяет, изменилась ли обложка
     */
    private fun hasCoverChanged(): Boolean {
        val currentUri = coverUri.value
        return currentUri != originalCoverUri
    }

    /**
     * Переопределяем проверку изменений для режима редактирования
     */
    override fun updateUnsavedChanges() {
        val currentName = _name.value ?: ""
        val currentDescription = _description.value ?: ""
        val currentCoverUri = coverUri.value

        // Проверяем, есть ли изменения по сравнению с оригинальными данными
        val nameChanged = currentName != originalName
        val descriptionChanged = currentDescription != originalDescription
        val coverChanged = currentCoverUri != originalCoverUri

        val newHasUnsavedChanges = nameChanged || descriptionChanged || coverChanged

        if (newHasUnsavedChanges != hasUnsavedChanges) {
            hasUnsavedChanges = newHasUnsavedChanges
            android.util.Log.d("EditPlaylistVM", "updateUnsavedChanges: hasUnsavedChanges=$hasUnsavedChanges, nameChanged=$nameChanged, descriptionChanged=$descriptionChanged, coverChanged=$coverChanged")
        }
    }

    /**
     * В режиме редактирования проверяем unsaved changes
     */
    override fun checkUnsavedChanges(): Boolean = hasUnsavedChanges

    companion object {
        private const val TAG = "EditPlaylistViewModel"
    }
}