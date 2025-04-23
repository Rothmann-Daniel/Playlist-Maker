package com.example.playlistmaker.track

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
        val isOnline = isNetworkAvailable(itemView.context)
        loadTrackImage(model, isOnline) // Загрузка обложки трека с обработкой ошибок
        setTrackTextInfo(model) // Установка текстовой информации
    }


    private fun loadTrackImage(model: Track, isOnline: Boolean) {
        val artworkUrl = if (isOnline) model.artworkUrl100 else null

        Glide.with(itemView)
            .load(artworkUrl)
            .placeholder(R.drawable.placeholder) // заглушка пока изображение загружается
            .error(if (isOnline) R.drawable.placeholder else R.drawable.placeholder) // Если загрузка не удалась заглушка для ошибок онлайн и офлайн
            .fallback(R.drawable.placeholder) // Если путь к изображению отсутствует/null
            .centerCrop()
            .transform(RoundedCorners(CORNER_RADIUS_DP.dpToPx())) // Динамическое скругление
            .transition(DrawableTransitionOptions.withCrossFade(FADE_DURATION_MS)) // Анимация
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(trackImageView)
    }


    // Устанавливает текстовую информацию о треке (название, исполнитель, время)
    private fun setTrackTextInfo(model: Track) {
        trackNameTextView.text =
            model.trackName ?: itemView.context.getString(R.string.unknown_track)
        trackArtistTextView.text =
            model.artistName ?: itemView.context.getString(R.string.unknown_artist)
        trackTimeTextView.text =
            model.trackTime   // при необходимости время возможно форматировать из миллисекунд в MM:SS
    }

    // Конвертирует dp в пиксели в зависимости от текущего масштаба
    private fun Int.dpToPx(): Int = (this * itemView.resources.displayMetrics.density).toInt()

    // Функция для проверки сети
    private fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            //Получаем текущую активную сеть (возвращает null если нет подключения)
            connectivityManager?.activeNetwork?.let { network ->
                connectivityManager.getNetworkCapabilities(network)?.run {
                    // true если есть мобильный интернет ИЛИ Wi-Fi
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                }
            } ?: false
        } catch (e: Exception) {
            false  // На случай если разрешение не предоставлено
        }
    }

}