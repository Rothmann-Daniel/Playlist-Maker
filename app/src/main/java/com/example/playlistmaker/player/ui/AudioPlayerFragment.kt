package com.example.playlistmaker.player.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentAudioPlayerBinding
import com.example.playlistmaker.search.domain.model.Track
import com.google.gson.Gson
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AudioPlayerFragment : Fragment(R.layout.fragment_audio_player) {

    private var _binding: FragmentAudioPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AudioPlayerViewModel by viewModel()
    private val gson: Gson by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAudioPlayerBinding.bind(view)

        initToolbar()
        initViews()
        setupObservers()
    }

    private fun initToolbar() {
        binding.toolbarAudioplayer.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initViews() {
        val track = getTrackFromArguments()
        loadTrackCover(track)
        setTrackInfo(track)

        track.previewUrl?.let { url ->
            viewModel.preparePlayer(url)
        } ?: run {
            Toast.makeText(requireContext(), "Preview not available", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp() // Исправлено
        }

        binding.ibPlayStop.setOnClickListener {
            viewModel.togglePlayPause()
        }
    }

    private fun getTrackFromArguments(): Track {
        val trackJson = arguments?.getString(TRACK_ARGUMENT)
            ?: throw IllegalArgumentException("Track data is missing")
        return gson.fromJson(trackJson, Track::class.java)
    }

    private fun loadTrackCover(track: Track) {
        val enlargedImageUrl = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
        val radiusInPx = (8f * resources.displayMetrics.density).toInt()

        Glide.with(requireContext())
            .load(enlargedImageUrl)
            .centerCrop()
            .transform(RoundedCorners(radiusInPx))
            .placeholder(R.drawable.placeholder)
            .into(binding.ivCover)
    }

    private fun setTrackInfo(track: Track) {
        binding.tvTrackName.text = track.trackName
        binding.tvArtistName.text = track.artistName
        binding.tvDurationValue.text = track.trackTimeMillis?.let { viewModel.formatTime(it) } ?: "--:--"
        binding.tvCollectionNameValue.text = track.collectionName ?: getString(R.string.unknown_collection)
        binding.tvReleaseDateValue.text = track.releaseDate?.take(4) ?: getString(R.string.unknown_year)
        binding.tvPrimaryGenreNameValue.text = track.primaryGenreName ?: getString(R.string.unknown_genre)
        binding.tvCountryValue.text = track.country ?: getString(R.string.unknown_country)
    }

    private fun setupObservers() {
        viewModel.playerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AudioPlayerViewModel.PlayerState.Preparing -> {
                    binding.ibPlayStop.isEnabled = false
                    binding.ibPlayStop.setImageResource(R.drawable.play)
                }
                is AudioPlayerViewModel.PlayerState.Prepared -> {
                    binding.ibPlayStop.isEnabled = true
                    binding.ibPlayStop.setImageResource(R.drawable.play)
                }
                is AudioPlayerViewModel.PlayerState.Playing -> {
                    binding.ibPlayStop.isEnabled = true
                    binding.ibPlayStop.setImageResource(R.drawable.pause)
                }
                is AudioPlayerViewModel.PlayerState.Paused -> {
                    binding.ibPlayStop.isEnabled = true
                    binding.ibPlayStop.setImageResource(R.drawable.play)
                }
                is AudioPlayerViewModel.PlayerState.Error -> {
                    binding.ibPlayStop.isEnabled = false
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.currentPosition.observe(viewLifecycleOwner) { position ->
            binding.tvTrackTime.text = position
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.playerState.value is AudioPlayerViewModel.PlayerState.Playing) {
            viewModel.pause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isRemoving) {
            viewModel.stop()
        }
        _binding = null
    }

    companion object {
        const val TRACK_ARGUMENT = "trackJson"

        fun newInstance(trackJson: String): AudioPlayerFragment {
            return AudioPlayerFragment().apply {
                arguments = Bundle().apply {
                    putString(TRACK_ARGUMENT, trackJson)
                }
            }
        }
    }
}
