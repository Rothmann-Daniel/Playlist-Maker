package com.example.playlistmaker.media.ui.editplaylist

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.media.ui.newplaylist.NewPlaylistFragment
import com.example.playlistmaker.media.ui.newplaylist.NewPlaylistViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File

/**
 * Fragment для редактирования плейлиста
 * Наследуется от NewPlaylistFragment и переопределяет поведение
 */
class EditPlaylistFragment : NewPlaylistFragment() {

    private val args: EditPlaylistFragmentArgs by navArgs()

    // Переопределяем ViewModel на EditPlaylistViewModel
    override val viewModel: EditPlaylistViewModel by viewModel {
        parametersOf(args.playlistId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Изменяем заголовок и текст кнопки
        setupEditMode()

        // Подписываемся на данные плейлиста
        observePlaylistData()
    }

    /**
     * Настройка UI для режима редактирования
     */
    private fun setupEditMode() {
        binding.toolbarNewPlayList.title = getString(R.string.edit_playlist_title)
        binding.createPlaylist.text = getString(R.string.save)
    }

    /**
     * Наблюдение за данными плейлиста
     */
    private fun observePlaylistData() {
        viewModel.playlist.observe(viewLifecycleOwner) { playlist ->
            playlist?.let {
                // Заполняем поля (они уже заполнены через ViewModel, но обложку нужно загрузить)
                it.coverImagePath?.let { coverPath ->
                    val coverFile = File(coverPath)
                    if (coverFile.exists()) {
                        Glide.with(requireContext())
                            .load(coverFile)
                            .centerCrop()
                            .placeholder(R.drawable.icon_add_photo)
                            .error(R.drawable.icon_add_photo)
                            .into(binding.playlistCover)
                    }
                }
            }
        }
    }

    /**
     * Переопределяем логику кнопки "Назад" - выход без подтверждения
     */
    override fun handleBackPress() {
        findNavController().navigateUp()
    }

    /**
     * Переопределяем клик на кнопку создания/сохранения
     */
    override fun setupClickListeners() {
        binding.playlistCover.setOnClickListener {
            pickImageFromGallery()
        }

        // Переопределяем действие кнопки - теперь обновляем плейлист
        binding.createPlaylist.setOnClickListener {
            viewModel.updatePlaylist()
        }
    }

    /**
     * Переопределяем наблюдение за состоянием - не показываем диалог выхода
     */
    override fun setupObservers() {
        viewModel.isCreateButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.createPlaylist.isEnabled = isEnabled
        }

        viewModel.createState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is NewPlaylistViewModel.CreatePlaylistState.Loading -> {
                    showLoading(true)
                }
                is NewPlaylistViewModel.CreatePlaylistState.Success -> {
                    showLoading(false)
                    showSuccessMessage(state.playlistName)
                    findNavController().navigateUp()
                }
                is NewPlaylistViewModel.CreatePlaylistState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                else -> {
                    showLoading(false)
                }
            }
        }

        // НЕ подписываемся на showExitDialog
    }

    override fun showSuccessMessage(playlistName: String) {
        val message = getString(R.string.playlist_updated_success, playlistName)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance() = EditPlaylistFragment()
    }
}