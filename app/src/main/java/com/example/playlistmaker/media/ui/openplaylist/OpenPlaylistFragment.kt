package com.example.playlistmaker.media.ui.openplaylist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentOpenPlaylistBinding
import com.example.playlistmaker.media.domain.model.Playlist
import com.example.playlistmaker.media.ui.playlist.PlaylistBottomSheetAdapter
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

    private lateinit var tracksBottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var menuBottomSheetBehavior: BottomSheetBehavior<*>

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
        setupBottomSheets()
        setupPlurals()
        setupClickListeners()
        setupObservers()

        viewModel.loadPlaylist(args.playlistId)
    }

    private fun setupToolbar() {
        binding.toolbarOpenplaylist.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupBottomSheets() {
        // Bottom Sheet для треков
        tracksBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetPlaylists)
        tracksBottomSheetBehavior.isHideable = false
        tracksBottomSheetBehavior.isFitToContents = false
        tracksBottomSheetBehavior.skipCollapsed = false
        val peekHeight = resources.getDimensionPixelSize(R.dimen.peekHeight_240)
        tracksBottomSheetBehavior.peekHeight = peekHeight
        tracksBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        tracksBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        // Показываем overlay только для треков, если меню скрыто
                        if (menuBottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                            binding.overlay.isVisible = true
                        }
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        // Скрываем overlay только если меню тоже скрыто
                        if (menuBottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                            binding.overlay.isVisible = false
                        }
                    }
                    else -> {}
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Обновляем прозрачность только если меню скрыто
                if (menuBottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                    binding.overlay.alpha = slideOffset.coerceAtLeast(0f)
                }
            }
        })

        // Bottom Sheet для меню
        menuBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetMenu)
        menuBottomSheetBehavior.isHideable = true
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        menuBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        // УБРАНО: не показываем overlay для меню
                        binding.overlay.isVisible = false
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        // УБРАНО: не показываем overlay для меню
                        binding.overlay.isVisible = false
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.overlay.isVisible = false
                        // Проверяем состояние треков
                        if (tracksBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                            binding.overlay.isVisible = true
                        }
                    }
                    else -> {}
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // не меняем прозрачность overlay для меню
            }
        })

        // Клик по overlay закрывает меню или сворачивает треки
        binding.overlay.setOnClickListener {
            if (menuBottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            } else if (tracksBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                tracksBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
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

    private fun setupClickListeners() {
        // Кнопка "Поделиться" на главном экране
        binding.shareIcon.setOnClickListener {
            sharePlaylist()
        }

        // Кнопка "Меню"
        binding.menuIcon.setOnClickListener {
            showMenu()
        }

        // Пункты меню
        binding.sharePlayList.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            sharePlaylist()
        }

        binding.removePlayList.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            showDeleteConfirmationDialog()
        }

        // Редактирование - пока не реализовано
        binding.editPlayList.setOnClickListener {
            Toast.makeText(requireContext(), "Функция в разработке", Toast.LENGTH_SHORT).show()
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

        viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it) {
                    Toast.makeText(
                        requireContext(),
                        "Плейлист удален",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Не удалось удалить плейлист",
                        Toast.LENGTH_SHORT
                    ).show()
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
                noTracksMessage.isVisible = true
                tracksListView.isVisible = false
            } else {
                noTracksMessage.isVisible = false
                tracksListView.isVisible = true
                setupTracksRecyclerView(state.tracks)
            }

            // Настройка меню с информацией о плейлисте
            setupMenuRecyclerView(state.playlist)
        }
    }

    private fun setupTracksRecyclerView(tracks: List<Track>) {
        val adapter = TrackAdapter(
            tracks = tracks,
            clickListener = { track -> onTrackClick(track) },
            longClickListener = { track -> showDeleteTrackDialog(track) }
        )

        binding.tracksListView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    private fun setupMenuRecyclerView(playlist: Playlist) {
        val adapter = PlaylistBottomSheetAdapter(
            playlists = listOf(playlist),
            onPlaylistClick = { /* Не нужно обрабатывать */ }
        )

        binding.playListView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    private fun showMenu() {
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun sharePlaylist() {
        val state = viewModel.state.value as? OpenPlaylistViewModel.PlaylistState.Content

        if (state == null || state.tracks.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "В этом плейлисте нет списка треков, которым можно поделиться",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val shareText = buildShareText(state.playlist, state.tracks)

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun buildShareText(playlist: Playlist, tracks: List<Track>): String {
        val sb = StringBuilder()

        // Название плейлиста
        sb.append(playlist.name)
        sb.append("\n")

        // Описание (если есть)
        if (!playlist.description.isNullOrEmpty()) {
            sb.append(playlist.description)
            sb.append("\n")
        }

        // Количество треков -  использование plural
        val tracksCountText = resources.getQuantityString(
            R.plurals.tracks_count,
            tracks.size,
            tracks.size // передаем количество как параметр
        )
        sb.append(tracksCountText)
        sb.append("\n\n")

        // Список треков
        tracks.forEachIndexed { index, track ->
            sb.append("${index + 1}. ")
            sb.append("${track.artistName} - ")
            sb.append(track.trackName)
            sb.append(" (${formatTrackTime(track.trackTimeMillis)})")
            if (index < tracks.size - 1) {
                sb.append("\n")
            }
        }

        return sb.toString()
    }

    private fun formatTrackTime(timeMillis: Long): String {
        val minutes = (timeMillis / 60000).toInt()
        val seconds = ((timeMillis % 60000) / 1000).toInt()
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun showDeleteConfirmationDialog() {
        val currentState = viewModel.state.value

        if (currentState !is OpenPlaylistViewModel.PlaylistState.Content) {
            Toast.makeText(requireContext(), "Ошибка: данные плейлиста не загружены", Toast.LENGTH_SHORT).show()
            return
        }

        val playlistName = currentState.playlist.name

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_playlist_title)
            .setMessage(getString(R.string.delete_playlist_message) + " \"$playlistName\"?")
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.yes) { dialog, _ ->
                viewModel.deletePlaylist()
                dialog.dismiss()
            }
            .show()
    }

    private fun showDeleteTrackDialog(track: Track) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.delete_track_message))
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.yes) { dialog, _ ->
                viewModel.deleteTrack(track.trackId)
                dialog.dismiss()
            }
            .show()
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
            Toast.makeText(
                requireContext(),
                "Не удалось открыть плеер",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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