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
                    // Сохраняем оригинальный путь к обложке
                    originalCoverPath = it.coverImagePath

                    // Заполняем поля формы
                    onNameChanged(it.name)
                    onDescriptionChanged(it.description ?: "")

                    // Устанавливаем URI обложки, если есть
                    it.coverImagePath?.let { path ->
                        // Конвертируем путь в URI для отображения
                        setCoverUri(Uri.parse("file://$path"))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Обновляет плейлист вместо создания нового
     */
    fun updatePlaylist() {
        val currentName = name.value?.trim()
        if (currentName.isNullOrBlank()) {
            _createState.value = CreatePlaylistState.Error("Название плейлиста не может быть пустым")
            return
        }

        _createState.value = CreatePlaylistState.Loading

        viewModelScope.launch {
            try {
                val playlist = _playlist.value ?: return@launch

                // Определяем путь к обложке
                val coverPath = when {
                    // Если выбрана новая обложка (URI изменился)
                    coverUri.value != null && coverUri.value.toString() != "file://$originalCoverPath" -> {
                        coverUri.value?.let { uri ->
                            // Сохраняем новую обложку
                            fileStorage.saveCoverImageFromUri(uri)
                        } ?: originalCoverPath
                    }
                    // Если обложка не изменилась
                    else -> originalCoverPath
                }

                // Создаем обновленный плейлист
                val updatedPlaylist = playlist.copy(
                    name = currentName,
                    description = description.value?.trim()?.takeIf { it.isNotEmpty() },
                    coverImagePath = coverPath
                )

                // Обновляем плейлист
                val result = playlistInteractor.updatePlaylist(updatedPlaylist)

                _createState.value = when {
                    result.isSuccess -> {
                        // Удаляем старую обложку, если была заменена
                        if (coverPath != originalCoverPath && originalCoverPath != null) {
                            fileStorage.deleteCoverImage(originalCoverPath)
                        }
                        CreatePlaylistState.Success(currentName)
                    }
                    else -> CreatePlaylistState.Error(
                        result.exceptionOrNull()?.message ?: "Неизвестная ошибка"
                    )
                }
            } catch (e: Exception) {
                _createState.value = CreatePlaylistState.Error(e.message ?: "Ошибка обновления")
            }
        }
    }

    /**
     * В режиме редактирования не показываем диалог выхода
     */
    override fun checkUnsavedChanges(): Boolean = false

    companion object {
        private const val TAG = "EditPlaylistViewModel"
    }
}