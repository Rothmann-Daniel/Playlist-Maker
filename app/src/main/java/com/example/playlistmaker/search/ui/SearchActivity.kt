package com.example.playlistmaker.search.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.player.ui.AudioPlayerActivity
import com.example.playlistmaker.search.domain.model.Track
import com.example.playlistmaker.search.ui.track.TrackAdapter

import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson

class SearchActivity : AppCompatActivity() {

    private val viewModel by viewModels<SearchViewModel> { ViewModelFactory(this) }

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

    private val tracksAdapter = TrackAdapter(emptyList()) { onTrackClick(it) }
    private val historyAdapter = TrackAdapter(emptyList()) { onTrackClick(it) }

    private var isClickDebounced = false
    private val clickDebounceHandler = Handler(Looper.getMainLooper())
    private val CLICK_DEBOUNCE_DELAY = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupListeners()
        observeViewModel()
        restoreState(savedInstanceState)

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

        trackList.adapter = tracksAdapter
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
                viewModel.updateHistoryVisibility(!s.isNullOrEmpty())

                if (!s.isNullOrEmpty()) {
                    viewModel.searchDebounced(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        clearButton.setOnClickListener { clearSearch() }
        updateButton.setOnClickListener { viewModel.retryLastSearch() }
        clearHistoryButton.setOnClickListener { viewModel.clearHistory() }

        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.searchImmediately(searchInput.text.toString())
                true
            } else false
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is SearchViewModel.SearchState.Loading -> showLoading()
                is SearchViewModel.SearchState.EmptyResult -> showEmptyResult()
                is SearchViewModel.SearchState.Content -> showTracks(state.tracks)
                is SearchViewModel.SearchState.Error -> showError(state.message)
                is SearchViewModel.SearchState.History -> showHistory(state.tracks)
            }
        }
    }

    private fun showLoading() {
        placeholderNoFound.visibility = View.GONE
        placeholderError.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        trackList.visibility = View.GONE
        historyPlaceholder.visibility = View.GONE
    }

    private fun showTracks(tracks: List<Track>) {
        tracksAdapter.updateTracks(tracks)
        placeholderNoFound.visibility = View.GONE
        placeholderError.visibility = View.GONE
        progressBar.visibility = View.GONE
        trackList.visibility = View.VISIBLE
        historyPlaceholder.visibility = View.GONE
    }

    private fun showEmptyResult() {
        placeholderNoFound.visibility = View.VISIBLE
        placeholderError.visibility = View.GONE
        progressBar.visibility = View.GONE
        trackList.visibility = View.GONE
        historyPlaceholder.visibility = View.GONE
    }

    private fun showError(errorMessage: String) {
        placeholderNoFound.visibility = View.GONE
        placeholderError.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        trackList.visibility = View.GONE
        historyPlaceholder.visibility = View.GONE
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun showHistory(tracks: List<Track>) {
        historyAdapter.updateTracks(tracks)
        placeholderNoFound.visibility = View.GONE
        placeholderError.visibility = View.GONE
        progressBar.visibility = View.GONE
        trackList.visibility = View.GONE
        historyPlaceholder.visibility = View.VISIBLE
    }

    private fun onTrackClick(track: Track) {
        if (isClickDebounced) return
        isClickDebounced = true

        startActivity(
            Intent(this, AudioPlayerActivity::class.java).apply {
                putExtra(TRACK_EXTRA, Gson().toJson(track))
            }
        )

        viewModel.addTrackToHistory(track)

        clickDebounceHandler.postDelayed({ isClickDebounced = false }, CLICK_DEBOUNCE_DELAY)
    }

    private fun clearSearch() {
        searchInput.text.clear()
        hideKeyBoard()
        viewModel.updateHistoryVisibility(false)
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

    override fun onResume() {
        super.onResume()
        if (searchInput.text.isEmpty()) {
            viewModel.updateHistoryVisibility(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clickDebounceHandler.removeCallbacksAndMessages(null)
    }

    companion object {
        const val TRACK_EXTRA = "trackJson"
        private const val INPUT_TEXT = "SEARCH_TEXT"
    }
}
