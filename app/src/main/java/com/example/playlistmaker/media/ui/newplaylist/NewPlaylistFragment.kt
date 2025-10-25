package com.example.playlistmaker.media.ui.newplaylist

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentNewPlaylistBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class NewPlaylistFragment : Fragment() {

    private var _binding: FragmentNewPlaylistBinding? = null
    private val binding get() = _binding!!

    private val gson: Gson by inject()
    private val viewModel: NewPlaylistViewModel by viewModel { parametersOf(gson) }

    // Launcher для выбора изображения
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.setCoverUri(uri)
                binding.playlistCover.setImageURI(uri)
            }
        }
    }

    // Launcher для запроса разрешений (Android 13+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Разрешение получено - открываем галерею
            launchGalleryIntent()
        } else {
            // Разрешение не получено - показываем объяснение
            showPermissionDeniedMessage()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupClickListeners()
        setupTextWatchers()
        setupObservers()
        // Устанавливаем фокус на поле ввода имени
        setFocusOnInputName()

        // Настройка скроллинга при открытой клавиатуре
        setupKeyboardScrolling()
    }

    private fun setFocusOnInputName() {
        binding.inputName.requestFocus()
        try {
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(binding.inputName, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        } catch (e: Exception) {
            // Игнорируем ошибки, связанные с показом клавиатуры
            e.printStackTrace()
        }
    }

    private fun setupKeyboardScrolling() {
        val container = binding.root as? NestedScrollView

        // Слушатель для автоматического скролла при фокусе
        binding.inputDescription.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Скроллим к полю описания с задержкой
                binding.inputDescription.postDelayed({
                    container?.scrollTo(0, binding.inputDescriptionLayout.bottom)
                }, 100)
            }
        }

        binding.inputName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.inputName.postDelayed({
                    container?.scrollTo(0, binding.inputNameLayout.top)
                }, 100)
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbarNewPlayList.setNavigationOnClickListener {
            handleBackPress()
        }
    }

    private fun setupClickListeners() {
        binding.playlistCover.setOnClickListener {
            pickImageFromGallery()
        }

        binding.createPlaylist.setOnClickListener {
            viewModel.createPlaylist()
        }
    }

    private fun setupTextWatchers() {
        binding.inputName.addTextChangedListener {
            viewModel.onNameChanged(it.toString())
        }

        binding.inputDescription.addTextChangedListener {
            viewModel.onDescriptionChanged(it.toString())
        }
    }

    private fun setupObservers() {
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

        viewModel.showExitDialog.observe(viewLifecycleOwner) { showDialog ->
            if (showDialog) {
                showExitConfirmationDialog()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBarContainer.isVisible = show
        binding.createPlaylist.isEnabled = !show
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccessMessage(playlistName: String) {
        val message = getString(R.string.playlist_created_success, playlistName)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Основной метод для выбора изображения с проверкой разрешений
     */
    private fun pickImageFromGallery() {
        // Проверяем разрешения в зависимости от версии Android
        if (hasReadMediaImagesPermission()) {
            // Разрешение есть - открываем галерею
            launchGalleryIntent()
        } else {
            // Разрешения нет - запрашиваем
            requestReadMediaImagesPermission()
        }
    }

    /**
     * Проверяет наличие разрешения на чтение медиа-файлов
     */
    private fun hasReadMediaImagesPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+) - READ_MEDIA_IMAGES
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 и ниже - разрешение не требуется для ACTION_GET_CONTENT
            true
        }
    }

    /**
     * Запрашивает разрешение на чтение медиа-файлов
     */
    private fun requestReadMediaImagesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Показываем диалог с объяснением перед запросом
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_MEDIA_IMAGES)) {
                showPermissionRationaleDialog()
            } else {
                // Запрашиваем разрешение напрямую
                requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
            }
        }
        // На Android 12 и ниже не запрашиваем - разрешение не требуется
    }

    /**
     * Показывает диалог с объяснением необходимости разрешения
     */
    private fun showPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.permission_required_title))
            .setMessage(getString(R.string.permission_required_message))
            .setPositiveButton(getString(R.string.continue_text)) { dialog, _ ->
                // Пользователь понял - запрашиваем разрешение
                requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Показывает сообщение об отказе в разрешении
     */
    private fun showPermissionDeniedMessage() {
        Toast.makeText(
            requireContext(),
            getString(R.string.permission_denied_message),
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Запускает интент для выбора изображения из галереи
     */
    private fun launchGalleryIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            // Опционально: ограничиваем типы файлов
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "image/jpeg",
                "image/jpg",
                "image/png",
                "image/webp"
            ))
        }
        pickImageLauncher.launch(intent)
    }

    private fun handleBackPress() {
        if (viewModel.checkUnsavedChanges()) {
            viewModel.showExitDialog()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.exit_dialog_title))
            .setMessage(getString(R.string.exit_dialog_message))
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                viewModel.hideExitDialog()
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.exit)) { dialog, _ ->
                viewModel.exitWithoutSaving()
                findNavController().navigateUp()
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = NewPlaylistFragment()

        // Константы для строк
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}