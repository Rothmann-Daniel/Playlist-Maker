package com.example.playlistmaker.media.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.PlaylistItemListViewBinding
import com.example.playlistmaker.media.domain.model.Playlist
import java.io.File

class PlaylistBottomSheetAdapter(
    private var playlists: List<Playlist> = emptyList(),
    private val onPlaylistClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistBottomSheetAdapter.PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = PlaylistItemListViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int = playlists.size

    fun updatePlaylists(newPlaylists: List<Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }

    inner class PlaylistViewHolder(
        private val binding: PlaylistItemListViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            with(binding) {
                val imageSize = itemView.resources.getDimensionPixelSize(R.dimen.track_image_size)

                if (!playlist.coverImagePath.isNullOrEmpty()) {
                    val coverFile = File(playlist.coverImagePath)
                    if (coverFile.exists()) {
                        Glide.with(itemView)
                            .load(coverFile)
                            .override(imageSize, imageSize) // ФИКСИРУЕМ РАЗМЕР
                            .centerCrop()
                            .transform(RoundedCorners(itemView.resources.getDimensionPixelSize(R.dimen.corner_radius)))
                            .placeholder(R.drawable.placeholder)
                            .into(playlistCover)
                    } else {
                        loadPlaceholder(imageSize)
                    }
                } else {
                    loadPlaceholder(imageSize)
                }

                playlistName.text = playlist.name
                val tracksText = itemView.resources.getQuantityString(
                    R.plurals.tracks_count,
                    playlist.tracksCount,
                    playlist.tracksCount
                )
                playlistTrackCount.text = tracksText

                itemView.setOnClickListener {
                    onPlaylistClick(playlist)
                }
            }
        }

        private fun loadPlaceholder(imageSize: Int) {
            Glide.with(itemView)
                .load(R.drawable.placeholder)
                .override(imageSize, imageSize)
                .centerCrop()
                .transform(RoundedCorners(itemView.resources.getDimensionPixelSize(R.dimen.corner_radius)))
                .into(binding.playlistCover)
        }
    }
}