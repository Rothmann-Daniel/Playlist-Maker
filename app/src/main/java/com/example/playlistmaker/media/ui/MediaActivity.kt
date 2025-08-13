package com.example.playlistmaker.media.ui

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityMediaBinding
import com.google.android.material.tabs.TabLayoutMediator


class MediaActivity : AppCompatActivity() {

    private var _binding: ActivityMediaBinding? = null

    private val binding get() = _binding!!

    private lateinit var tabMediator: TabLayoutMediator

    //Создаем адаптер напрямую
    private val adapter by lazy { MediaViewPagerAdapter(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Обработка кнопки "Назад" в Toolbar
        binding.toolBarMedia.setNavigationOnClickListener {
            finish()
        }

        //Обработчик нажатия на системную кнопку навигаци: Назад
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        //Настройка ViewPager и TabLayout

        binding.viewPager.adapter = adapter

        tabMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.favorite_tracks)
                1 -> tab.text = getString(R.string.play_lists)
            }
        }
        tabMediator.attach()
    }


    //Очистка при уничтожении
    override fun onDestroy() {
        super.onDestroy()
        tabMediator.detach()
    }

}

