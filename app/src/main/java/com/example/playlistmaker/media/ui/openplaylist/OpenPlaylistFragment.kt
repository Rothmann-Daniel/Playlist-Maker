package com.example.playlistmaker.media.ui.openplaylist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentOpenPlaylistBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class OpenPlaylistFragment : Fragment() {

    private var _binding: FragmentOpenPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OpenPlaylistViewModel by viewModel()
    private val args: OpenPlaylistFragmentArgs by navArgs()

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
        setupBottomSheetBehavior() // Настройка Bottom Sheet
        setupObservers()

        // Загружаем данные плейлиста
        viewModel.loadPlaylist(args.playlistId)
    }

    private fun setupToolbar() {
        binding.toolbarOpenplaylist.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupBottomSheetBehavior() {
        // Инициализируем BottomSheetBehavior
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetPlaylists)

        // Настройка поведения
        bottomSheetBehavior.isHideable = true // Можно полностью скрыть
        bottomSheetBehavior.isFitToContents = false
        bottomSheetBehavior.skipCollapsed = false // Позволяет останавливаться на peekHeight

        // Устанавливаем высоту в свернутом состоянии (peek height)
        val peekHeight = resources.getDimensionPixelSize(R.dimen.peekHeight_240)
        bottomSheetBehavior.peekHeight = peekHeight

        // Начальное состояние - свернуто до peekHeight
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        // Слушатель изменений состояния Bottom Sheet
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        // Полностью открыт
                        binding.overlay.isVisible = true // Показываем оверлей
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        // Свернут до peekHeight
                        binding.overlay.isVisible = false // Скрываем оверлей
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        // Полураскрытое состояние (если используется)
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        // Полностью скрыт
                        binding.overlay.isVisible = false
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        // Пользователь тянет Bottom Sheet
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                        // Bottom Sheet анимируется к конечному состоянию
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Анимация при скольжении (slideOffset от -1 до 1)
                // Можно анимировать оверлей в зависимости от позиции
                binding.overlay.alpha = slideOffset.coerceAtLeast(0f)
            }
        })

        // Обработчик клика на оверлей для скрытия Bottom Sheet
        binding.overlay.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
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
            // Устанавливаем обложку
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

            // Устанавливаем название
            tvPlayListName.text = state.playlist.name

            // Устанавливаем описание (скрываем если пусто)
            if (!state.playlist.description.isNullOrEmpty()) {
                tvDescripcionPlayList.text = state.playlist.description
                tvDescripcionPlayList.isVisible = true
            } else {
                tvDescripcionPlayList.isVisible = false
            }

            // Устанавливаем длительность и количество треков
            playlistDuration.text = state.totalDuration
            playlistTrackCount.text = state.tracksCount

            // Обновляем список треков в Bottom Sheet
            // TODO: Добавь адаптер для RecyclerView когда будешь готов
            // if (state.tracks.isNotEmpty()) {
            //     tracksListView.adapter = TrackAdapter(state.tracks) { track ->
            //         // Обработка клика по треку
            //     }
            // }
        }
    }

    private fun showError(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    // Дополнительные методы для управления Bottom Sheet из кода
    fun expandBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun collapseBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun hideBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = OpenPlaylistFragment()
    }
}