package com.example.playlistmaker

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.model.Track
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale

class AudioPlayer : AppCompatActivity() {

    // MediaPlayer и состояние воспроизведения
    private var mediaPlayer: MediaPlayer? = null

    private var playerState = STATE_DEFAULT

    // Handler и Runnable для обновления времени трека
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateTimeRunnable: Runnable

    // UI элементы
    private lateinit var playButton: ImageButton
    private lateinit var trackTimeTextView: TextView
    private val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        initToolbar()
        initViews()
        setupPlayButton()
        initTimeUpdater()
    }

    private fun initToolbar() {
        // Инициализация Toolbar с кнопкой "Назад"
        findViewById<MaterialToolbar>(R.id.toolbar_audioplayer).apply {
            setNavigationOnClickListener { finish() }
        }
    }

    private fun initViews() {
        // Получаем данные трека из Intent
        val track = getTrackFromIntent()

        // Загрузка обложки трека
        loadTrackCover(track)

        // Установка текстовых данных
        setTrackInfo(track)
    }

    private fun getTrackFromIntent(): Track {
        val trackJson = intent.getStringExtra("trackJson") ?: ""
        return Gson().fromJson(trackJson, Track::class.java)
    }

    private fun loadTrackCover(track: Track) {
        val ivCover = findViewById<ImageView>(R.id.iv_Cover)
        val enlargedImageUrl = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
        val radiusInPx = (8f * resources.displayMetrics.density).toInt()

        Glide.with(this)
            .load(enlargedImageUrl)
            .centerCrop()
            .transform(RoundedCorners(radiusInPx))
            .placeholder(R.drawable.placeholder)
            .into(ivCover)
    }

    private fun setTrackInfo(track: Track) {
        findViewById<TextView>(R.id.tv_TrackName).text = track.trackName
        findViewById<TextView>(R.id.tv_ArtistName).text = track.artistName
        findViewById<TextView>(R.id.tv_DurationValue).text =
            dateFormat.format(track.trackTimeMillis)
        findViewById<TextView>(R.id.tv_CollectionNameValue).text = track.collectionName
        findViewById<TextView>(R.id.tv_ReleaseDateValue).text = track.releaseDate?.substring(0, 4)
        findViewById<TextView>(R.id.tv_PrimaryGenreNameValue).text = track.primaryGenreName
        findViewById<TextView>(R.id.tv_CountryValue).text = track.country
    }

    private fun setupPlayButton() {
        playButton = findViewById(R.id.ib_Play_Stop)
        trackTimeTextView = findViewById(R.id.tv_TrackTime)

        playButton.setOnClickListener {
            when (playerState) {
                STATE_PLAYING -> pauseAudio()
                STATE_PREPARED, STATE_PAUSED -> startAudio()
                else -> preparePlayer()
            }
        }
    }

    private fun initTimeUpdater() {
        // Инициализация Runnable для обновления времени
        updateTimeRunnable = object : Runnable {
            override fun run() {
                updateTrackTime()
                handler.postDelayed(this, UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun preparePlayer() {
        val track = getTrackFromIntent()
        track.previewUrl?.let { url ->
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(url)
                    prepareAsync()

                    setOnPreparedListener {
                        playButton.isEnabled = true
                        playerState = STATE_PREPARED
                    }

                    setOnCompletionListener {
                        completeAudioPlayback()
                    }

                    setOnErrorListener { _, what, extra ->
                        Log.e("MediaPlayer", "Error occurred: what=$what, extra=$extra")
                        playButton.isEnabled = false
                        playerState = STATE_DEFAULT
                        false // возвращаем false, чтобы не вызывать OnCompletionListener
                    }
                }
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Error preparing media player", e)
                playButton.isEnabled = false
                playerState = STATE_DEFAULT
            }
        } ?: run {
            playButton.isEnabled = false
            Log.e("MediaPlayer", "Preview URL is null")
        }
    }

    private fun startAudio() {
        mediaPlayer?.start()
        playerState = STATE_PLAYING
        playButton.setImageResource(R.drawable.pause)
        handler.post(updateTimeRunnable)
    }

    private fun pauseAudio() {
        mediaPlayer?.pause()
        playerState = STATE_PAUSED
        playButton.setImageResource(R.drawable.play)
        handler.removeCallbacks(updateTimeRunnable)
    }

    private fun stopAudio() {
        mediaPlayer?.release()
        mediaPlayer = null
        playerState = STATE_DEFAULT
        playButton.setImageResource(R.drawable.play)
        trackTimeTextView.text = "00:00"
        handler.removeCallbacks(updateTimeRunnable)
    }

    private fun completeAudioPlayback() {
        playButton.setImageResource(R.drawable.play)
        playerState = STATE_PREPARED
        stopTimeUpdater()
        trackTimeTextView.text = "00:00"
    }

    private fun updateTrackTime() {
        mediaPlayer?.let { player ->
            trackTimeTextView.text = dateFormat.format(player.currentPosition)
        }
    }

    private fun stopTimeUpdater() {
        handler.removeCallbacks(updateTimeRunnable)
    }

    override fun onPause() {
        super.onPause()
        if (playerState == STATE_PLAYING) {
            pauseAudio()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAudio()
    }

    // Состояния плеера
    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val UPDATE_INTERVAL_MS = 100L
    }
}