package com.example.playlistmaker.player.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityAudioPlayerBinding
import com.example.playlistmaker.search.domain.model.Track
import com.google.gson.Gson
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioPlayerBinding
    private val viewModel: AudioPlayerViewModel by viewModel() // Используем Koin ViewModel
    private val gson: Gson by inject() // Добавляем получение Gson из Koin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initViews()
        setupObservers()
    }

    private fun initToolbar() {
        binding.toolbarAudioplayer.setNavigationOnClickListener { finish() }
    }

    private fun initViews() {
        val track = getTrackFromIntent()
        loadTrackCover(track)
        setTrackInfo(track)

        track.previewUrl?.let { url ->
            viewModel.preparePlayer(url)
        } ?: run {
            Toast.makeText(this, "Preview not available", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.ibPlayStop.setOnClickListener {
            viewModel.togglePlayPause()
        }
    }

    private fun getTrackFromIntent(): Track {
        val trackJson = intent.getStringExtra("trackJson")
            ?: throw IllegalArgumentException("Track data is missing")
        return gson.fromJson(trackJson, Track::class.java) // Используем внедренный Gson
    }

    private fun loadTrackCover(track: Track) {
        val enlargedImageUrl = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
        val radiusInPx = (8f * resources.displayMetrics.density).toInt()

        Glide.with(this)
            .load(enlargedImageUrl)
            .centerCrop()
            .transform(RoundedCorners(radiusInPx))
            .placeholder(R.drawable.placeholder)
            .into(binding.ivCover)
    }

    private fun setTrackInfo(track: Track) {
        binding.tvTrackName.text = track.trackName
        binding.tvArtistName.text = track.artistName
        binding.tvDurationValue.text = track.trackTimeMillis?.let { viewModel.formatTime(it) } ?: "--:--"
        binding.tvCollectionNameValue.text = track.collectionName ?: getString(R.string.unknown_collection)
        binding.tvReleaseDateValue.text = track.releaseDate?.take(4) ?: getString(R.string.unknown_year)
        binding.tvPrimaryGenreNameValue.text = track.primaryGenreName ?: getString(R.string.unknown_genre)
        binding.tvCountryValue.text = track.country ?: getString(R.string.unknown_country)
    }

    private fun setupObservers() {
        viewModel.playerState.observe(this) { state ->
            when (state) {
                is AudioPlayerViewModel.PlayerState.Preparing -> {
                    binding.ibPlayStop.isEnabled = false
                    binding.ibPlayStop.setImageResource(R.drawable.play)
                }
                is AudioPlayerViewModel.PlayerState.Prepared -> {
                    binding.ibPlayStop.isEnabled = true
                    binding.ibPlayStop.setImageResource(R.drawable.play)
                }
                is AudioPlayerViewModel.PlayerState.Playing -> {
                    binding.ibPlayStop.isEnabled = true
                    binding.ibPlayStop.setImageResource(R.drawable.pause)
                }
                is AudioPlayerViewModel.PlayerState.Paused -> {
                    binding.ibPlayStop.isEnabled = true
                    binding.ibPlayStop.setImageResource(R.drawable.play)
                }
                is AudioPlayerViewModel.PlayerState.Error -> {
                    binding.ibPlayStop.isEnabled = false
                    Toast.makeText(this@AudioPlayerActivity, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.currentPosition.observe(this) { position ->
            binding.tvTrackTime.text = position
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.playerState.value is AudioPlayerViewModel.PlayerState.Playing) {
            viewModel.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            viewModel.stop()
        }
    }
}