package com.example.playlistmaker.media.ui.editplaylist



import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.playlistmaker.R
import com.example.playlistmaker.media.ui.newplaylist.NewPlaylistFragment
import com.example.playlistmaker.media.ui.newplaylist.NewPlaylistViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File

class EditPlaylistFragment : NewPlaylistFragment() {

    private val args: EditPlaylistFragmentArgs by navArgs()

    private val editViewModel: EditPlaylistViewModel by viewModel {
        parametersOf(args.playlistId)
    }

    override val viewModel get() = editViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEditModeUI()
        setupEditObservers()

        android.util.Log.d("EditPlaylistFragment", "Fragment created, observing button state")
    }

    private fun setupEditModeUI() {
        binding.toolbarNewPlayList.title = getString(R.string.edit_playlist)
        binding.createPlaylist.text = getString(R.string.save)

        android.util.Log.d("EditPlaylistFragment", "Edit mode UI setup completed")
    }

    private fun setupEditObservers() {
        editViewModel.playlist.observe(viewLifecycleOwner) { playlist ->
            playlist?.let {
                android.util.Log.d("EditPlaylistFragment", "Playlist loaded: ${it.name}")

                // --- Предзаполнение полей UI ---
                // Заполнение полей Название и Описание
                binding.inputName.setText(it.name)
                binding.inputDescription.setText(it.description)

                // Заполнение обложки
                it.coverImagePath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        binding.playlistCover.setImageURI(Uri.fromFile(file))
                    }
                }

                android.util.Log.d("EditPlaylistFragment", "Button enabled after data load: ${binding.createPlaylist.isEnabled}")
            }
        }

        editViewModel.isCreateButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            android.util.Log.d("EditPlaylistFragment", "Button enabled state changed: $isEnabled")
            binding.createPlaylist.isEnabled = isEnabled
        }

        editViewModel.createState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is NewPlaylistViewModel.CreatePlaylistState.Success -> {
                    showSuccessMessage(state.playlistName)
                    // Возвращаемся к экрану плейлиста с обновлением
                    try {
                        findNavController().popBackStack(R.id.openPlaylistFragment, false)
                    } catch (e: Exception) {
                        android.util.Log.e("EditPlaylistFragment", "Navigation error: $e")
                        findNavController().navigateUp()
                    }
                }
                else -> {
                    // Базовая обработка уже в родительском классе
                }
            }
        }
    }

    /**
     * Переопределяем логику обработки нажатия "Назад" (системная кнопка/жест)
     * В режиме редактирования изменения игнорируются без подтверждения.
     */
    override fun handleBackPress() {
        findNavController().navigateUp()
    }

    /**
     * Переопределяем логику нажатия на кнопку в тулбаре "Назад"
     */
    override fun setupToolbar() {
        binding.toolbarNewPlayList.setNavigationOnClickListener {
            handleBackPress() // Вызовет переопределенный метод
        }
    }


    override fun setupClickListeners() {
        // Вызываем родительский метод, чтобы сохранить слушатель для обложки,
        // но переопределяем слушатель для кнопки "Сохранить"
        super.setupClickListeners()

        binding.createPlaylist.setOnClickListener {
            android.util.Log.d("EditPlaylistFragment", "Save button clicked, enabled: ${binding.createPlaylist.isEnabled}")
            editViewModel.updatePlaylist()
        }
    }

    override fun showSuccessMessage(playlistName: String) {
        val message = getString(R.string.playlist_updated_success, playlistName)
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance() = EditPlaylistFragment()
    }
}