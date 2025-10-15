package com.example.playlistmaker.media.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.PlaylistItemGridViewBinding
import com.example.playlistmaker.media.domain.model.Playlist
import java.io.File

class PlaylistAdapter(
    private var playlists: List<Playlist> = emptyList(),
    private val onPlaylistClick: (Playlist) -> Unit = {}
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = PlaylistItemGridViewBinding.inflate(
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
        private val binding: PlaylistItemGridViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            with(binding) {
                // Устанавливаем обложку
                if (!playlist.coverImagePath.isNullOrEmpty()) {
                    val coverFile = File(playlist.coverImagePath)
                    if (coverFile.exists()) {
                        Glide.with(itemView)
                            .load(coverFile)
                            .transform(RoundedCorners(16))
                            .placeholder(R.drawable.placeholder)
                            .into(playlistCover)
                    } else {
                        playlistCover.setImageResource(R.drawable.placeholder)
                    }
                } else {
                    playlistCover.setImageResource(R.drawable.placeholder)
                }

                // Устанавливаем название
                playlistName.text = playlist.name

                // Устанавливаем количество треков
                val tracksText = itemView.resources.getQuantityString(
                    R.plurals.tracks_count,
                    playlist.tracksCount,
                    playlist.tracksCount
                )
                playlistTrackCount.text = tracksText

                // Обработка клика
                itemView.setOnClickListener {
                    onPlaylistClick(playlist)
                }
            }
        }
    }
}