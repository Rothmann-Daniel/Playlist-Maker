package com.example.playlistmaker.media.ui

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityMediaBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaActivity : AppCompatActivity() {

    private val mediaViewModel: MediaViewModel by viewModel()
    private var _binding: ActivityMediaBinding? = null
    private val binding get() = _binding!!
    private lateinit var tabMediator: TabLayoutMediator

    private val adapter by lazy { MediaViewPagerAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupBackPressHandler()
        setupViewPager()
        observeTabSelection()
    }
    // Инициализация Toolbar и обработка нажатия кнопки "назад"
    private fun setupToolbar() {
        binding.toolBarMedia.setNavigationOnClickListener {
            finish()
        }
    }
    // Обработка нажатия кнопки "назад"
    private fun setupBackPressHandler() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    // Инициализация ViewPager и TabLayout
    private fun setupViewPager() {
        binding.viewPager.adapter = adapter

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
    // Обновляем ViewModel при смене таба
    private fun observeTabSelection() {
        // Дополнительная подписка на изменения (если нужно синхронизировать с другими компонентами)
        mediaViewModel.selectedTab
            .onEach { position ->
                if (binding.viewPager.currentItem != position) {
                    binding.viewPager.setCurrentItem(position, false)
                }
            }
            .launchIn(lifecycleScope)
    }

    // Очистка ресурсов
    override fun onDestroy() {
        super.onDestroy()
        binding.viewPager.adapter = null
        tabMediator.detach()
        _binding = null
    }
}