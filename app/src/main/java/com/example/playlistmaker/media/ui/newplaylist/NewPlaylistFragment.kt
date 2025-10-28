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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
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

open class NewPlaylistFragment : Fragment() {

    protected var _binding: FragmentNewPlaylistBinding? = null
    protected val binding get() = _binding!!

    private val gson: Gson by inject()

    protected open val viewModel: NewPlaylistViewModel by viewModel { parametersOf(gson) }

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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchGalleryIntent()
        } else {
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
        setFocusOnInputName()
        setupKeyboardScrolling()
        setupKeyboardInsets()

        // Логируем конечное состояние
        binding.createPlaylist.post {
            android.util.Log.d("NewPlaylistFragment", "Final button state after setup: ${binding.createPlaylist.isEnabled}")
        }
    }

    protected open fun setupToolbar() {
        binding.toolbarNewPlayList.setNavigationOnClickListener {
            handleBackPress()
        }
    }

    protected open fun setupClickListeners() {
        binding.playlistCover.setOnClickListener {
            pickImageFromGallery()
        }

        binding.createPlaylist.setOnClickListener {
            viewModel.createPlaylist()
        }
    }

    protected open fun setupTextWatchers() {
        binding.inputName.addTextChangedListener {
            viewModel.onNameChanged(it.toString())
        }

        binding.inputDescription.addTextChangedListener {
            viewModel.onDescriptionChanged(it.toString())
        }
    }

    protected open fun setupObservers() {
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

    private fun setFocusOnInputName() {
        binding.inputName.requestFocus()
        try {
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(binding.inputName, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupKeyboardScrolling() {
        binding.inputDescription.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.inputDescription.postDelayed({
                    binding.scrollView.scrollTo(0, binding.inputDescriptionLayout.bottom)
                }, 100)
            }
        }

        binding.inputName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.inputName.postDelayed({
                    binding.scrollView.scrollTo(0, binding.inputNameLayout.top)
                }, 100)
            }
        }
    }

    protected open fun showLoading(show: Boolean) {
        binding.progressBarContainer.isVisible = show
        // Не меняем состояние кнопки здесь - этим управляет ViewModel через isCreateButtonEnabled
    }

    protected open fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    protected open fun showSuccessMessage(playlistName: String) {
        val message = getString(R.string.playlist_created_success, playlistName)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    protected fun pickImageFromGallery() {
        if (hasReadMediaImagesPermission()) {
            launchGalleryIntent()
        } else {
            requestReadMediaImagesPermission()
        }
    }

    private fun hasReadMediaImagesPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestReadMediaImagesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_MEDIA_IMAGES)) {
                showPermissionRationaleDialog()
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.permission_required_title))
            .setMessage(getString(R.string.permission_required_message))
            .setPositiveButton(getString(R.string.continue_text)) { dialog, _ ->
                requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(
            requireContext(),
            getString(R.string.permission_denied_message),
            Toast.LENGTH_LONG
        ).show()
    }

    protected fun launchGalleryIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "image/jpeg",
                "image/jpg",
                "image/png",
                "image/webp"
            ))
        }
        pickImageLauncher.launch(intent)
    }

    protected open fun handleBackPress() {
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

    private fun setupKeyboardInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomButtonContainer) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(bottom = imeInsets.bottom + systemBars.bottom)
            insets
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = NewPlaylistFragment()
    }
}