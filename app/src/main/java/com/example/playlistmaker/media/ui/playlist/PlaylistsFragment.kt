package com.example.playlistmaker.media.ui.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistsBinding
import com.example.playlistmaker.media.ui.mediafragment.MediaFragmentDirections
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistsViewModel by viewModel()

    private lateinit var playlistsAdapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun setupRecyclerView() {
        // Инициализация адаптера с обработчиком клика
        playlistsAdapter = PlaylistAdapter { playlistId ->
            navigateToPlaylist(playlistId)
        }

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.playlistsGrid.apply {
            layoutManager = gridLayoutManager
            adapter = playlistsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.newPlaylistButton.setOnClickListener {
            navigateToNewPlaylist()
        }
    }

    private fun navigateToNewPlaylist() {
        try {
            requireParentFragment().findNavController()
                .navigate(R.id.action_mediaFragment_to_newPlayList)
        } catch (e: Exception) {
            try {
                findNavController().navigate(R.id.newPlayList)
            } catch (e2: Exception) {
                findNavController().navigate(R.id.newPlayList)
            }
        }
    }

    private fun navigateToPlaylist(playlistId: Long) {
        try {
            // Используем Safe Args для передачи аргументов
            val action = MediaFragmentDirections
                .actionMediaFragmentToOpenPlaylist(playlistId)

            requireParentFragment().findNavController().navigate(action)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlaylistsViewModel.PlaylistsState.Empty -> {
                    showEmptyState()
                }
                is PlaylistsViewModel.PlaylistsState.Content -> {
                    showPlaylists(state.playlists)
                }
            }
        }
    }

    private fun showEmptyState() {
        binding.playlistsGrid.isVisible = false
        binding.placeholder.isVisible = true
    }

    private fun showPlaylists(playlists: List<com.example.playlistmaker.media.domain.model.Playlist>) {
        binding.playlistsGrid.isVisible = true
        binding.placeholder.isVisible = false
        playlistsAdapter.updatePlaylists(playlists)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}