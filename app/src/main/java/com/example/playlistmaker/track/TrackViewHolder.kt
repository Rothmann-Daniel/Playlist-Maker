package com.example.playlistmaker.track

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.playlistmaker.R

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val trackImageView: ImageView
    private val trackNameTextView: TextView
    private val trackArtistTextView: TextView
    private val dotImageView: ImageView
    private val trackTimeTextView: TextView
    private val forwardImageView: ImageView

    companion object {
        private const val CORNER_RADIUS_DP = 2 // Радиус скругления углов в dp
        private const val FADE_DURATION_MS = 300 // Длительность анимации появления
    }


    init {
        trackImageView = itemView.findViewById(R.id.track_image)
        trackNameTextView = itemView.findViewById(R.id.track_name)
        trackArtistTextView = itemView.findViewById(R.id.track_artist)
        dotImageView = itemView.findViewById(R.id.dot_separator)
        trackTimeTextView = itemView.findViewById(R.id.track_time)
        forwardImageView = itemView.findViewById(R.id.track_info)
    }

    // Загрузка обложки трека с обработкой ошибок и анимациями при загрузке

    fun bind(model: Track) {
        loadTrackImage(model) // Загрузка обложки трека
        setTrackTextInfo(model) // Установка текстовой информации

    }

    private fun loadTrackImage(model: Track) {
        Glide.with(itemView)
            .load(model.artworkUrl100?.takeIf { it.isNotBlank() }) // Проверка на пустой URL
            .placeholder(R.drawable.search)// Заглушка во время загрузки
            .error(R.drawable.error_53) // Заглушка при ошибке
            .fallback(R.drawable.error_404) // Если URL null
            .centerCrop()
            .transform(RoundedCorners(CORNER_RADIUS_DP.dpToPx())) // Динамическое скругление
            .transition(DrawableTransitionOptions.withCrossFade(FADE_DURATION_MS)) // Анимация
            .into(trackImageView)
    }

    // Устанавливает текстовую информацию о треке (название, исполнитель, время)
    private fun setTrackTextInfo(model: Track) {
        trackNameTextView.text = model.trackName ?: itemView.context.getString(R.string.unknown_track)
        trackArtistTextView.text = model.artistName ?: itemView.context.getString(R.string.unknown_artist)
        trackTimeTextView.text = model.trackTime   // при необходимости время возможно форматировать из миллисекунд в MM:SS
    }
    // Конвертирует dp в пиксели в зависимости от текущего масштаба
    private fun Int.dpToPx(): Int = (this * itemView.resources.displayMetrics.density).toInt()

}