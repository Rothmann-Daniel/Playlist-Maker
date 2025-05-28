package com.example.playlistmaker.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.AudioPlayer
import com.example.playlistmaker.R
import com.example.playlistmaker.model.Track
import com.example.playlistmaker.model.TrackResponse
import com.example.playlistmaker.network.iTunesAPI
import com.example.playlistmaker.trackAdapter.TrackAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    // Retrofit и API
    private val trackBaseURL = "https://itunes.apple.com"
    private val retrofit = Retrofit.Builder()
        .baseUrl(trackBaseURL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val trackService = retrofit.create(iTunesAPI::class.java)

    // UI элементы
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

    // Адаптеры и данные
    private val tracks = ArrayList<Track>()
    private val adapter = TrackAdapter()
    private val historyAdapter = TrackAdapter()
    private lateinit var searchHistory: SearchHistory

    // Переменные состояния
    private var searchText: String = SAVED_TEXT
    private var lastInput: String? = null

    companion object {
        const val INPUT_TEXT = "SEARCH_TEXT"
        const val SAVED_TEXT = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupAdapters()
        setupSearchHistory()
        setupListeners()
        restoreState(savedInstanceState)

        // Показываем историю сразу при открытии, если поле пустое и есть сохраненные треки
        updateHistoryVisibility()

        // Устанавливаем фокус на поле ввода
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
    }

    private fun setupAdapters() {
        adapter.tracks = tracks
        trackList.adapter = adapter

        historyRecyclerView.adapter = historyAdapter

        // Обработчик кликов для основного списка треков
        adapter.setOnTrackClickListener(object : TrackAdapter.OnTrackClickListener {
            override fun onTrackClick(track: Track) {
                searchHistory.addTrack(track)
                val intent = Intent(this@SearchActivity, AudioPlayer::class.java).apply {
                    val gson = Gson()
                    val trackJson = gson.toJson(track)
                    putExtra("trackJson", trackJson)
                }
                startActivity(intent)
            }
        })

        // Обработчик кликов для истории
        historyAdapter.setOnTrackClickListener(object : TrackAdapter.OnTrackClickListener {
            override fun onTrackClick(track: Track) {
                searchHistory.addTrack(track)
                val intent = Intent(this@SearchActivity, AudioPlayer::class.java).apply {
                    val gson = Gson()
                    val trackJson = gson.toJson(track)
                    putExtra("trackJson", trackJson)
                }
                startActivity(intent)
            }
        })
    }

    private fun setupSearchHistory() {
        searchHistory = SearchHistory(getSharedPreferences("playlist_maker_prefs", MODE_PRIVATE))
        updateHistoryVisibility()
    }

    private fun setupListeners() {
        // Навигация назад
        findViewById<MaterialToolbar>(R.id.tool_bar_search).setNavigationOnClickListener {
            finish()
        }

        // Слушатель текста в поле поиска
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                searchText = s?.toString() ?: ""
                updateHistoryVisibility()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Клик по полю поиска - показать клавиатуру
        searchInput.setOnClickListener {
            // Показываем клавиатуру, но не меняем видимость истории
            showKeyBoard()
        }

        // Кнопка очистки поиска
        clearButton.setOnClickListener {
            searchInput.text.clear()
            hideKeyBoard()
            tracks.clear()
            adapter.notifyDataSetChanged()
            placeholderNoFound.visibility = View.GONE
            updateHistoryVisibility()
        }

        // Обработка нажатия Done на клавиатуре
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch(searchInput.text.toString())
                true
            } else {
                false
            }
        }

        // Кнопка обновления при ошибке
        updateButton.setOnClickListener {
            lastInput?.let { input ->
                performSearch(input)
            }
        }

        // Кнопка очистки истории
        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            updateHistoryVisibility()
        }

        // Отслеживание фокуса в поле поиска
        searchInput.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            // Обновляем видимость истории только при потере фокуса
            if (!hasFocus) {
                updateHistoryVisibility(hasFocus)
            }
        }
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(INPUT_TEXT, SAVED_TEXT)
            searchInput.setText(searchText)
        }
    }

    private fun updateHistoryVisibility(hasFocus: Boolean = searchInput.hasFocus()) {
        val history = searchHistory.getHistory()
        val showHistory = searchInput.text.isEmpty() && history.isNotEmpty()

        historyPlaceholder.visibility = if (showHistory) View.VISIBLE else View.GONE
        trackList.visibility = if (showHistory) View.GONE else View.VISIBLE

        if (showHistory) {
            historyAdapter.tracks.clear()
            historyAdapter.tracks.addAll(history)
            historyAdapter.notifyDataSetChanged()
        }
    }

    private fun performSearch(query: String) {
        lastInput = query
        placeholderNoFound.visibility = View.GONE
        placeholderError.visibility = View.GONE
        trackList.visibility = View.VISIBLE

        trackService.search(query).enqueue(object : Callback<TrackResponse> {
            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                if (response.isSuccessful) {
                    val results = response.body()?.results ?: emptyList()
                    handleSearchResults(ArrayList(results)) // Явное преобразование в ArrayList
                } else {
                    handleSearchError()
                }
            }

            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                handleSearchError()
            }
        })
    }

    private fun handleSearchResults(results: ArrayList<Track>) {
        tracks.clear()
        if (results.isNotEmpty()) {
            tracks.addAll(results)
            adapter.notifyDataSetChanged()
            placeholderNoFound.visibility = View.GONE
            placeholderError.visibility = View.GONE
            trackList.visibility = View.VISIBLE
        } else {
            placeholderNoFound.visibility = View.VISIBLE
            placeholderError.visibility = View.GONE
            trackList.visibility = View.GONE
        }
    }

    private fun handleSearchError() {
        tracks.clear()
        adapter.notifyDataSetChanged()
        placeholderNoFound.visibility = View.GONE
        placeholderError.visibility = View.VISIBLE
        trackList.visibility = View.GONE
    }


    private fun showKeyBoard() {
        // 1. Устанавливаем фокус на поле ввода
        searchInput.requestFocus()

        // 2. Получаем сервис управления клавиатурой
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // 3. Показываем клавиатуру с проверкой:
        // - что поле ввода имеет фокус
        // - что view видимо и прикреплено к window
        if (searchInput.isFocused && searchInput.windowToken != null) {
            imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideKeyBoard() {
        (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow(searchInput.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INPUT_TEXT, searchText)
    }
}