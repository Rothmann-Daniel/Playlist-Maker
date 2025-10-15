package com.example.playlistmaker.player.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentAudioPlayerBinding
import com.example.playlistmaker.media.domain.model.Playlist
import com.example.playlistmaker.media.ui.PlaylistBottomSheetAdapter
import com.example.playlistmaker.search.domain.model.Track
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AudioPlayerFragment : Fragment(R.layout.fragment_audio_player) {

    private var _binding: FragmentAudioPlayerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AudioPlayerViewModel by viewModel()
    private val gson: Gson by inject()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<android.widget.LinearLayout>
    private lateinit var playlistsAdapter: PlaylistBottomSheetAdapter
    private var currentTrack: Track? = null
    private var bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAudioPlayerBinding.bind(view)

        initToolbar()
        initViews()
        setupBottomSheet()
        setupObservers()
    }

    private fun initToolbar() {
        binding.toolbarAudioplayer.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initViews() {
        currentTrack = getTrackFromArguments()
        currentTrack?.let { track ->
            viewModel.setTrack(track)
            loadTrackCover(track)
            setTrackInfo(track)

            // Настройка кнопки избранного
            binding.addToFavoriteButton.setOnClickListener {
                viewModel.onFavoriteClicked()
            }

            // Настройка кнопки добавления в плейлист
            binding.addToPlayListButton.setOnClickListener {
                showPlaylistsBottomSheet()
            }

            // Воспроизведение трека
            track.previewUrl?.let { url ->
                viewModel.preparePlayer(url)
            } ?: run {
                Toast.makeText(requireContext(), "Preview not available", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }

            binding.ibPlayStop.setOnClickListener {
                viewModel.togglePlayPause()
            }
        } ?: run {
            Toast.makeText(requireContext(), "Track data is missing", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun setupBottomSheet() {
        // Инициализация Bottom Sheet Behavior
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetPlaylists)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // Настройка адаптера для списка плейлистов
        playlistsAdapter = PlaylistBottomSheetAdapter(
            onPlaylistClick = { playlist ->
                currentTrack?.let { track ->
                    viewModel.addTrackToPlaylist(track, playlist)
                }
            }
        )

        binding.PlaylistsListView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = playlistsAdapter
        }

        // Обработчик состояний Bottom Sheet
        bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.overlay.visibility = View.GONE
                        viewModel.clearAddToPlaylistResult()
                    }
                    BottomSheetBehavior.STATE_EXPANDED,
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.overlay.visibility = View.VISIBLE
                    }
                    else -> {
                        // Обработка других состояний
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val alpha = when {
                    slideOffset < 0 -> 0f
                    slideOffset > 1 -> 1f
                    else -> slideOffset
                }
                binding.overlay.alpha = alpha
            }
        }
        bottomSheetCallback?.let { bottomSheetBehavior.addBottomSheetCallback(it) }

        // Кнопка "Новый плейлист"
        binding.newPlaylistButton.setOnClickListener {
            navigateToNewPlaylist()
        }

        // Overlay для закрытия Bottom Sheet
        binding.overlay.setOnClickListener {
            hidePlaylistsBottomSheet()
        }

        // Обработка свайпа для закрытия Bottom Sheet
        binding.bottomSheetPlaylists.setOnTouchListener { _, event ->
            // Дополнительная логика обработки свайпа (опционально)
            false
        }
    }

    private fun setupObservers() {
        // Observer для состояния плеера
        viewModel.playerState.observe(viewLifecycleOwner) { state ->
            if (!isAdded) return@observe

            when (state) {
                is AudioPlayerViewModel.PlayerState.Preparing -> {
                    binding.ibPlayStop.isEnabled = false
                    binding.ibPlayStop.setImageResource(R.drawable.play)
                    binding.tvTrackTime.text = "00:00"
                }

                is AudioPlayerViewModel.PlayerState.Prepared -> {
                    binding.ibPlayStop.isEnabled = true
                    binding.ibPlayStop.setImageResource(R.drawable.play)
                    binding.tvTrackTime.text = "00:00"
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
                    binding.ibPlayStop.setImageResource(R.drawable.play)
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observer для текущей позиции воспроизведения
        viewModel.currentPosition.observe(viewLifecycleOwner) { position ->
            if (!isAdded) return@observe
            binding.tvTrackTime.text = position
        }

        // Observer для состояния избранного
        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            if (!isAdded) return@observe
            val favoriteIcon = if (isFavorite) {
                R.drawable.favourite_fill
            } else {
                R.drawable.favourite
            }
            binding.addToFavoriteButton.setImageResource(favoriteIcon)
        }

        // Observer для состояния плейлистов
        viewModel.playlistsState.observe(viewLifecycleOwner) { state ->
            if (!isAdded) return@observe

            when (state) {
                is AudioPlayerViewModel.PlaylistsState.Loading -> {
                    showPlaylistsLoading(true)
                }

                is AudioPlayerViewModel.PlaylistsState.Empty -> {
                    showPlaylistsLoading(false)
                    playlistsAdapter.updatePlaylists(emptyList())
                    showEmptyPlaylistsState(true)
                }

                is AudioPlayerViewModel.PlaylistsState.Content -> {
                    showPlaylistsLoading(false)
                    playlistsAdapter.updatePlaylists(state.playlists)
                    showEmptyPlaylistsState(false)
                }

                is AudioPlayerViewModel.PlaylistsState.Error -> {
                    showPlaylistsLoading(false)
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    hidePlaylistsBottomSheet()
                }
            }
        }

        // Observer для результата добавления в плейлист
        viewModel.addToPlaylistResult.observe(viewLifecycleOwner) { result ->
            if (!isAdded) return@observe

            result?.let {
                when (it) {
                    is AudioPlayerViewModel.AddToPlaylistResult.Success -> {
                        hidePlaylistsBottomSheet()
                        Toast.makeText(
                            requireContext(),
                            "Добавлено в плейлист ${it.playlistName}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is AudioPlayerViewModel.AddToPlaylistResult.AlreadyExists -> {
                        Toast.makeText(
                            requireContext(),
                            "Трек уже добавлен в плейлист ${it.playlistName}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is AudioPlayerViewModel.AddToPlaylistResult.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                }
                viewModel.clearAddToPlaylistResult()
            }
        }
    }

    private fun showPlaylistsBottomSheet() {
        if (!isAdded) return
        viewModel.loadPlaylists()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        binding.overlay.visibility = View.VISIBLE
    }

    private fun hidePlaylistsBottomSheet() {
        if (!isAdded) return
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        binding.overlay.visibility = View.GONE
    }

    private fun navigateToNewPlaylist() {
        if (!isAdded) return
        hidePlaylistsBottomSheet()

        // Переходим к созданию нового плейлиста
        try {
            findNavController().navigate(R.id.action_audioPlayerFragment_to_newPlayList)
        } catch (e: Exception) {
            // Если action не найден, используем прямой переход
            findNavController().navigate(R.id.newPlayList)
        }
    }

    private fun getTrackFromArguments(): Track? {
        return try {
            val trackJson = arguments?.getString(TRACK_ARGUMENT)
                ?: return null
            gson.fromJson(trackJson, Track::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun loadTrackCover(track: Track) {
        if (!isAdded) return

        try {
            val enlargedImageUrl = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
            val radiusInPx = (8f * resources.displayMetrics.density).toInt()

            Glide.with(requireContext())
                .load(enlargedImageUrl)
                .centerCrop()
                .transform(RoundedCorners(radiusInPx))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(binding.ivCover)
        } catch (e: Exception) {
            if (isAdded) {
                binding.ivCover.setImageResource(R.drawable.placeholder)
            }
        }
    }

    private fun setTrackInfo(track: Track) {
        if (!isAdded) return

        binding.tvTrackName.text = track.trackName
        binding.tvArtistName.text = track.artistName
        binding.tvDurationValue.text = track.trackTimeMillis?.let { viewModel.formatTime(it) } ?: "--:--"
        binding.tvCollectionNameValue.text = track.collectionName ?: getString(R.string.unknown_collection)
        binding.tvReleaseDateValue.text = track.releaseDate?.take(4) ?: getString(R.string.unknown_year)
        binding.tvPrimaryGenreNameValue.text = track.primaryGenreName ?: getString(R.string.unknown_genre)
        binding.tvCountryValue.text = track.country ?: getString(R.string.unknown_country)
    }

    private fun showPlaylistsLoading(show: Boolean) {
        if (!isAdded) return
        binding.PlaylistsListView.visibility = if (show) View.INVISIBLE else View.VISIBLE
    }

    private fun showEmptyPlaylistsState(show: Boolean) {
        if (!isAdded) return
        // Можно добавить TextView для пустого состояния в разметку Bottom Sheet
        if (show) {
            // Показать сообщение "Нет плейлистов"
        } else {
            // Скрыть сообщение
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

        // Очищаем колбэки Bottom Sheet чтобы избежать NPE
        bottomSheetCallback?.let { bottomSheetBehavior.removeBottomSheetCallback(it) }

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