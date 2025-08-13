package com.example.playlistmaker.media.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityMediaBinding
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


class MediaActivity : AppCompatActivity() {

    private var _binding: ActivityMediaBinding? = null

    private val binding get() = _binding!!

    private lateinit var tabMediator: TabLayoutMediator

    //  создание адаптера через Koin
    private val adapter: MediaViewPagerAdapter by inject { parametersOf(supportFragmentManager, lifecycle) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Обработчик нажатия на кнопку навигаци: Назад
        binding.toolBarMedia.setNavigationOnClickListener {
            finish()
        }

        binding.viewPager.adapter = adapter

        tabMediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.favorite_tracks)
                1 -> tab.text = getString(R.string.play_lists)
            }
        }
        tabMediator.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        tabMediator.detach()
    }

}

