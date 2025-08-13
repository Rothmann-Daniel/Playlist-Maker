package com.example.playlistmaker.media.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MediaViewPagerAdapter(
    activity: FragmentActivity
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2 // Количество вкладок

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FavoriteTracksFragment.newInstance()
            1 -> PlaylistsFragment.newInstance()
            else -> PlaylistsFragment.newInstance()
        }
    }
}