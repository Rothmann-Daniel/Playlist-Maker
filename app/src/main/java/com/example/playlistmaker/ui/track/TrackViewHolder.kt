package com.example.playlistmaker.ui.track

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import java.text.SimpleDateFormat
import java.util.Locale

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val trackImageView: ImageView
    private val trackNameTextView: TextView
    private val trackArtistTextView: TextView
    private val dotImageView: ImageView
    private val trackTimeTextView: TextView
    private val forwardImageView: ImageView

    init {
        trackImageView = itemView.findViewById(R.id.track_image)
        trackNameTextView = itemView.findViewById(R.id.track_name)
        trackArtistTextView = itemView.findViewById(R.id.track_artist)
        dotImageView = itemView.findViewById(R.id.dot_separator)
        trackTimeTextView = itemView.findViewById(R.id.track_time)
        forwardImageView = itemView.findViewById(R.id.track_info)
    }

    fun bind(model: Track) {
        Glide.with(itemView)
            .load(model.artworkUrl100)
            .placeholder(R.drawable.placeholder)
            .centerCrop()
            .transform(RoundedCorners(2))
            .into(trackImageView)

        trackNameTextView.text = model.trackName
        trackArtistTextView.text = model.artistName
        trackTimeTextView.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(model.trackTimeMillis)
    }
}
