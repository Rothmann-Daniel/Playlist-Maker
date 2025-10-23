package com.example.playlistmaker.media.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentFavoriteTracksBinding
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.ui.track.TrackAdapter
import com.google.gson.Gson
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoriteTracksFragment : Fragment() {

    private var _binding: FragmentFavoriteTracksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoriteTracksViewModel by viewModel()
    private val gson: Gson by inject()

    private val tracksAdapter = TrackAdapter(emptyList()) { onTrackClick(it) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteTracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        // Обновляем список при возврате на экран
        viewModel.refresh()
    }

    private fun setupRecyclerView() {
        binding.favoriteTracksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tracksAdapter
        }
    }

    private fun setupObservers() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavoriteTracksViewModel.FavoriteTracksState.Empty -> {
                    showEmptyState()
                }
                is FavoriteTracksViewModel.FavoriteTracksState.Content -> {
                    showTracks(state.tracks)
                }
            }
        }
    }

    private fun showEmptyState() {
        binding.favoriteTracksRecyclerView.isVisible = false
        binding.emptyStateContainer.isVisible = true
    }

    private fun showTracks(tracks: List<Track>) {
        binding.favoriteTracksRecyclerView.isVisible = true
        binding.emptyStateContainer.isVisible = false
        tracksAdapter.updateTracks(tracks)
    }

    private fun onTrackClick(track: Track) {
        navigateToPlayer(track)
    }

    private fun navigateToPlayer(track: Track) {
        val bundle = Bundle().apply {
            putString("trackJson", gson.toJson(track))
        }
        try {
            // Используем NavController родительского фрагмента (MediaFragment)
            requireParentFragment().findNavController()
                .navigate(R.id.audioPlayerFragment, bundle)
        } catch (e: Exception) {
            // Обработка ошибки навигации
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = FavoriteTracksFragment()
    }
}