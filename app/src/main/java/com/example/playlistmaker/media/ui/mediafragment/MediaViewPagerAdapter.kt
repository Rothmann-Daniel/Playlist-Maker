package com.example.playlistmaker.media.ui.mediafragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.playlistmaker.media.ui.playlist.PlaylistsFragment
import com.example.playlistmaker.media.ui.favorite.FavoriteTracksFragment

class MediaViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 2 // Количество вкладок

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FavoriteTracksFragment.newInstance()
            1 -> PlaylistsFragment.newInstance()
            else -> PlaylistsFragment.newInstance()
        }
    }
}