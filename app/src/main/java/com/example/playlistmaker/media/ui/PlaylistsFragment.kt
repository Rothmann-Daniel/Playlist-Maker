package com.example.playlistmaker.media.ui

import PlaylistAdapter
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
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistsViewModel by viewModel()

    private val playlistsAdapter = PlaylistAdapter()

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
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.playlistsGrid.apply {
            layoutManager = gridLayoutManager
            adapter = playlistsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.newPlaylistButton.setOnClickListener {
            // Используем родительский фрагмент для навигации
            navigateToNewPlaylist()
        }
    }

    private fun navigateToNewPlaylist() {
        try {
            // Способ 1: Через родительский фрагмент (MediaFragment)
            requireParentFragment().findNavController().navigate(R.id.action_mediaFragment_to_newPlayList)
        } catch (e: Exception) {
            // Способ 2: Альтернативный подход
            try {
                findNavController().navigate(R.id.newPlayList)
            } catch (e2: Exception) {
                // Способ 3: Используем прямой ID назначения
                findNavController().navigate(R.id.newPlayList)
            }
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