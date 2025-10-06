package com.example.playlistmaker.media.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentMediaBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaFragment : Fragment(R.layout.fragment_media) {

    private var _binding: FragmentMediaBinding? = null
    private val binding get() = _binding!!
    private val mediaViewModel: MediaViewModel by viewModel()
    private lateinit var tabMediator: TabLayoutMediator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMediaBinding.bind(view)

        setupViewPager()
        observeTabSelection()
    }

    private fun setupViewPager() {
        // Создаем адаптер
        val adapter = MediaViewPagerAdapter(childFragmentManager, lifecycle)
        binding.viewPager.adapter = adapter

        // КЛЮЧЕВОЕ РЕШЕНИЕ: отключаем автоматическое сохранение состояния ViewPager2
        binding.viewPager.isSaveEnabled = false

        // Восстанавливаем сохраненную позицию
        binding.viewPager.setCurrentItem(mediaViewModel.selectedTab.value, false)

        // Обновляем ViewModel при смене таба
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                mediaViewModel.selectTab(position)
            }
        })

        // Инициализируем TabLayout
        tabMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.favorite_tracks)
                1 -> getString(R.string.play_lists)
                else -> ""
            }
        }
        tabMediator.attach()
    }

    private fun observeTabSelection() {
        mediaViewModel.selectedTab
            .onEach { position ->
                if (binding.viewPager.currentItem != position) {
                    binding.viewPager.setCurrentItem(position, false)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::tabMediator.isInitialized) {
            tabMediator.detach()
        }
        _binding = null
    }
}