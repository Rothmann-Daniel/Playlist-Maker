package com.example.playlistmaker

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.model.Track
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale

class AudioPlayer : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_audio_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Включаем кнопку "Назад" в Toolbar
        val toolbar = findViewById<ImageButton>(R.id.toolbar_audioplayer)
        toolbar.setOnClickListener {
            finish()
        }

        val trackJson = intent.getStringExtra("trackJson")
        val gson = Gson()
        val track: Track = gson.fromJson(trackJson, Track::class.java)
        Log.d("trackJson", trackJson.toString())

        val ivCover = findViewById<ImageView>(R.id.iv_Cover)
        val tvTrackName = findViewById<TextView>(R.id.tv_TrackName)
        val tvArtistName = findViewById<TextView>(R.id.tv_ArtistName)
        val tvDurationValue = findViewById<TextView>(R.id.tv_DurationValue)
        val tvCollectionNameValue = findViewById<TextView>(R.id.tv_CollectionNameValue)
        val tvReleaseDateValue = findViewById<TextView>(R.id.tv_ReleaseDateValue)
        val tvPrimaryGenreNameValue = findViewById<TextView>(R.id.tv_PrimaryGenreNameValue)
        val tvCountryValue = findViewById<TextView>(R.id.tv_CountryValue)

        val enlargedImageUrl = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")

        // Конвертируем 8dp в пиксели для Glide
        val radiusInPx = (8f * resources.displayMetrics.density).toInt()

        Glide.with(this)
            .load(enlargedImageUrl)
            .centerCrop()
            .transform(RoundedCorners(radiusInPx))  // Теперь 8dp
            .placeholder(R.drawable.placeholder)
            .into(ivCover)

        tvTrackName.text = track.trackName
        tvArtistName.text = track.artistName
        tvDurationValue.text =
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)
        tvCollectionNameValue.text = track.collectionName
        tvReleaseDateValue.text = track.releaseDate?.substring(0, 4)
        tvPrimaryGenreNameValue.text = track.primaryGenreName
        tvCountryValue.text = track.country
    }
}

