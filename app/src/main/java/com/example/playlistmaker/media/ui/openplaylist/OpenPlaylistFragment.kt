package com.example.playlistmaker.media.ui.openplaylist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentOpenPlaylistBinding
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.ui.track.TrackAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class OpenPlaylistFragment : Fragment() {

    private var _binding: FragmentOpenPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OpenPlaylistViewModel by viewModel()
    private val args: OpenPlaylistFragmentArgs by navArgs()
    private val gson: Gson by inject()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOpenPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupBottomSheetBehavior()
        setupPlurals()
        setupObservers()

        viewModel.loadPlaylist(args.playlistId)
    }

    private fun setupToolbar() {
        binding.toolbarOpenplaylist.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupBottomSheetBehavior() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetPlaylists)

        bottomSheetBehavior.isHideable = false
        bottomSheetBehavior.isFitToContents = false
        bottomSheetBehavior.skipCollapsed = false

        val peekHeight = resources.getDimensionPixelSize(R.dimen.peekHeight_240)
        bottomSheetBehavior.peekHeight = peekHeight
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.overlay.isVisible = true
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.overlay.isVisible = false
                    }
                    else -> {}
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.overlay.alpha = slideOffset.coerceAtLeast(0f)
            }
        })

        binding.overlay.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun setupPlurals() {
        viewModel.tracksCountPlurals = { count ->
            resources.getQuantityString(R.plurals.tracks_count, count, count)
        }
        viewModel.minutesCountPlurals = { minutes ->
            resources.getQuantityString(R.plurals.minutes_count, minutes, minutes)
        }
    }

    private fun setupObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is OpenPlaylistViewModel.PlaylistState.Loading -> {
                    showLoading(true)
                }
                is OpenPlaylistViewModel.PlaylistState.Content -> {
                    showLoading(false)
                    displayPlaylist(state)
                }
                is OpenPlaylistViewModel.PlaylistState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
        binding.scrollView.isVisible = !show
        binding.bottomSheetPlaylists.isVisible = !show
    }

    private fun displayPlaylist(state: OpenPlaylistViewModel.PlaylistState.Content) {
        with(binding) {
            // Обложка
            if (!state.playlist.coverImagePath.isNullOrEmpty()) {
                val coverFile = File(state.playlist.coverImagePath)
                if (coverFile.exists()) {
                    Glide.with(requireContext())
                        .load(coverFile)
                        .centerCrop()
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(ivCoverPlaylist)
                } else {
                    ivCoverPlaylist.setImageResource(R.drawable.placeholder)
                }
            } else {
                ivCoverPlaylist.setImageResource(R.drawable.placeholder)
            }

            // Название
            tvPlayListName.text = state.playlist.name

            // Описание
            if (!state.playlist.description.isNullOrEmpty()) {
                tvDescripcionPlayList.text = state.playlist.description
                tvDescripcionPlayList.isVisible = true
            } else {
                tvDescripcionPlayList.isVisible = false
            }

            // Длительность и количество
            playlistDuration.text = state.totalDuration
            playlistTrackCount.text = state.tracksCount

            // Проверяем наличие треков
            if (state.tracks.isEmpty()) {
                // Показываем сообщение о пустом плейлисте
                noTracksMessage.isVisible = true
                tracksListView.isVisible = false
            } else {
                // Показываем список треков
                noTracksMessage.isVisible = false
                tracksListView.isVisible = true
                setupTracksRecyclerView(state.tracks)
            }
        }
    }

    private fun setupTracksRecyclerView(tracks: List<Track>) {
        val adapter = TrackAdapter(
            tracks = tracks,
            clickListener = { track -> onTrackClick(track) },
            longClickListener = { track -> showDeleteDialog(track) }
        )

        binding.tracksListView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    private fun onTrackClick(track: Track) {
        navigateToPlayer(track)
    }

    private fun navigateToPlayer(track: Track) {
        val bundle = Bundle().apply {
            putString("trackJson", gson.toJson(track))
        }
        try {
            findNavController().navigate(
                R.id.action_openPlaylistFragment_to_audioPlayerFragment,
                bundle
            )
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                requireContext(),
                "Не удалось открыть плеер",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showDeleteDialog(track: Track) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage("Хотите удалить трек?")
            .setNegativeButton("НЕТ") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("ДА") { dialog, _ ->
                viewModel.deleteTrack(track.trackId)
                dialog.dismiss()
            }
            .show()
    }

    private fun showError(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = OpenPlaylistFragment()
    }
}