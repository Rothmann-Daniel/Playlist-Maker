package com.example.playlistmaker.presentation.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.InteractorCreator
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.audio_player.AudioPlayer
import com.example.playlistmaker.presentation.track.TrackAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import kotlinx.coroutines.launch
import com.example.playlistmaker.domain.model.NetworkResult

class SearchActivity : AppCompatActivity() {

    // UseCases
    private val searchUseCase = InteractorCreator.searchTracksUseCase
    private val addToHistoryUseCase = InteractorCreator.addTrackToHistoryUseCase
    private val getHistoryUseCase = InteractorCreator.getSearchHistoryUseCase
    private val clearHistoryUseCase = InteractorCreator.clearSearchHistoryUseCase

    // Views
    private lateinit var searchInput: EditText
    private lateinit var clearButton: ImageView
    private lateinit var placeholderNoFound: LinearLayout
    private lateinit var placeholderError: LinearLayout
    private lateinit var updateButton: Button
    private lateinit var trackList: RecyclerView
    private lateinit var historyPlaceholder: LinearLayout
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var clearHistoryButton: Button
    private lateinit var historyTitle: TextView
    private lateinit var progressBar: FrameLayout

    // Data
    private val tracks = ArrayList<Track>()
    private val adapter = TrackAdapter(tracks) { onTrackClick(it) }
    private val historyAdapter = TrackAdapter(ArrayList()) { onTrackClick(it) }

    // Debounce
    private val searchDebounceHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val SEARCH_DEBOUNCE_DELAY = 2000L
    private var isClickDebounced = false
    private val CLICK_DEBOUNCE_DELAY = 1000L
    private var lastInput: String? = null

    // State
    private var currentDebounceRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupListeners()
        restoreState(savedInstanceState)
        updateHistoryVisibility()

        searchInput.post {
            searchInput.requestFocus()
            showKeyBoard()
        }
    }

    private fun initViews() {
        searchInput = findViewById(R.id.searchInput)
        clearButton = findViewById(R.id.button_clear)
        updateButton = findViewById(R.id.button_update)
        placeholderNoFound = findViewById(R.id.notFound_placeholder)
        placeholderError = findViewById(R.id.error_placeholder)
        trackList = findViewById(R.id.recyclerTrackList)
        historyPlaceholder = findViewById(R.id.history_placeholder)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
        historyTitle = findViewById(R.id.historyTitle)
        progressBar = findViewById(R.id.progressBarContainer)

        trackList.adapter = adapter
        historyRecyclerView.adapter = historyAdapter
    }

    private fun setupListeners() {
        findViewById<MaterialToolbar>(R.id.tool_bar_search).setNavigationOnClickListener {
            finish()
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                updateHistoryVisibility()

                searchRunnable?.let { searchDebounceHandler.removeCallbacks(it) }

                if (!s.isNullOrEmpty()) {
                    searchRunnable = Runnable { performSearch(s.toString()) }
                    searchDebounceHandler.postDelayed(searchRunnable!!, SEARCH_DEBOUNCE_DELAY)
                } else {
                    clearSearchResults()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        clearButton.setOnClickListener { clearSearch() }
        updateButton.setOnClickListener { lastInput?.let { performSearch(it) } }
        clearHistoryButton.setOnClickListener { clearHistory() }

        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchRunnable?.let { searchDebounceHandler.removeCallbacks(it) }
                performSearch(searchInput.text.toString())
                true
            } else false
        }
    }

    private fun onTrackClick(track: Track) {
        if (isClickDebounced) return
        isClickDebounced = true

        // Запускаем добавление в историю (без ожидания завершения)
        lifecycleScope.launch {
            try {
                addToHistoryUseCase(track)
            } catch (e: Exception) {
                Log.e("SearchActivity", "Failed to add track to history", e)
            }
        }

        // Открываем плеер без задержки
        startActivity(
            Intent(this, AudioPlayer::class.java).apply {
                putExtra(TRACK_EXTRA, Companion.gson.toJson(track))
            }
        )

        // Сбрасываем debounce флаг с задержкой
        val debounceRunnable = Runnable { isClickDebounced = false }
        searchDebounceHandler.postDelayed(debounceRunnable, CLICK_DEBOUNCE_DELAY)
        // Сохраняем Runnable для отмены в onDestroy
        currentDebounceRunnable = debounceRunnable
    }

    private fun performSearch(query: String) {
        lastInput = query
        showLoading()

        lifecycleScope.launch {
            when (val result = searchUseCase(query)) {
                is NetworkResult.Success<List<Track>> -> {
                    tracks.clear()
                    tracks.addAll(result.data)
                    adapter.notifyDataSetChanged()
                    showTracks()
                }
                is NetworkResult.Failure -> {
                    showError(result.error)
                }
            }
        }
    }

    private fun updateHistoryVisibility() {
        lifecycleScope.launch {
            val history = getHistoryUseCase()
            runOnUiThread {
                val showHistory = searchInput.text.isEmpty() && history.isNotEmpty()
                historyPlaceholder.visibility = if (showHistory) View.VISIBLE else View.GONE
                trackList.visibility = if (showHistory) View.GONE else View.VISIBLE

                if (showHistory) {
                    historyAdapter.updateTracks(history)
                }
            }
        }
    }

    private fun clearHistory() {
        lifecycleScope.launch {
            clearHistoryUseCase()
            updateHistoryVisibility()
        }
    }

    private fun clearSearch() {
        searchInput.text.clear()
        hideKeyBoard()
        clearSearchResults()
    }

    private fun clearSearchResults() {
        tracks.clear()
        adapter.notifyDataSetChanged()
        placeholderNoFound.visibility = View.GONE
        placeholderError.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun showLoading() {
        placeholderNoFound.visibility = View.GONE
        placeholderError.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        trackList.visibility = View.GONE
    }

    private fun showTracks() {
        adapter.notifyDataSetChanged()
        placeholderNoFound.visibility = if (tracks.isEmpty()) View.VISIBLE else View.GONE
        placeholderError.visibility = View.GONE
        progressBar.visibility = View.GONE
        trackList.visibility = View.VISIBLE
    }

    private fun showError(errorMessage: String) {
        runOnUiThread {
            tracks.clear()
            adapter.notifyDataSetChanged()
            placeholderNoFound.visibility = View.GONE
            placeholderError.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            Toast.makeText(this@SearchActivity, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showKeyBoard() {
        searchInput.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyBoard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchInput.windowToken, 0)
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        savedInstanceState?.getString(INPUT_TEXT)?.let {
            searchInput.setText(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INPUT_TEXT, searchInput.text.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        currentDebounceRunnable?.let { searchDebounceHandler.removeCallbacks(it) }
        searchDebounceHandler.removeCallbacksAndMessages(null)
    }

    companion object {
        const val TRACK_EXTRA = "trackJson"
        private const val INPUT_TEXT = "SEARCH_TEXT"
        val gson = Gson()
    }
}