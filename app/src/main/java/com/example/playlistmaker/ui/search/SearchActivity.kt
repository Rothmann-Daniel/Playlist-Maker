package com.example.playlistmaker.ui.search

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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.ui.audio_player.AudioPlayer
import com.example.playlistmaker.R
import com.example.playlistmaker.data.dto.TrackDto
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.data.dto.TrackResponse
import com.example.playlistmaker.data.dto.toTrack
import com.example.playlistmaker.data.network.NetworkClient
import com.example.playlistmaker.data.network.RetrofitNetworkClient
import com.example.playlistmaker.data.network.iTunesAPI
import com.example.playlistmaker.ui.track.TrackAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

//    // Retrofit и API
//    private val trackBaseURL = "https://itunes.apple.com"
//    private val retrofit = Retrofit.Builder()
//        .baseUrl(trackBaseURL)
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//    private val trackService = retrofit.create(iTunesAPI::class.java)

    private val networkClient: NetworkClient = RetrofitNetworkClient() // Инициализация клиента

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
    private lateinit var progressBar: FrameLayout

    // Адаптеры и данные
    private val tracks = ArrayList<Track>()
    private val adapter = TrackAdapter()
    private val historyAdapter = TrackAdapter()
    private lateinit var searchHistory: SearchHistory

    // Переменные состояния
    private var searchText: String = SAVED_TEXT
    private var lastInput: String? = null

    // Debounce для поиска
    private val searchDebounceHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val SEARCH_DEBOUNCE_DELAY = 2000L // 2 секунды задержки

    // Debounce для кликов
    private var isClickDebounced = false
    private val CLICK_DEBOUNCE_DELAY = 1000L // 1 секунда задержки

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
        progressBar = findViewById(R.id.progressBarContainer)
    }

    private fun setupAdapters() {
        adapter.tracks = tracks
        trackList.adapter = adapter
        historyRecyclerView.adapter = historyAdapter

        // Обработчик кликов для основного списка треков с debounce
        adapter.setOnTrackClickListener(object : TrackAdapter.OnTrackClickListener {
            override fun onTrackClick(track: Track) {
                if (isClickDebounced) return
                isClickDebounced = true

                searchHistory.addTrack(track)
                val intent = Intent(this@SearchActivity, AudioPlayer::class.java).apply {
                    val gson = Gson()
                    val trackJson = gson.toJson(track)
                    putExtra(TRACK_EXTRA, trackJson)
                }
                startActivity(intent)

                // Сбрасываем флаг через заданное время
                searchDebounceHandler.postDelayed({
                    isClickDebounced = false
                }, CLICK_DEBOUNCE_DELAY)
            }
        })

        // Обработчик кликов для истории с debounce
        historyAdapter.setOnTrackClickListener(object : TrackAdapter.OnTrackClickListener {
            override fun onTrackClick(track: Track) {
                if (isClickDebounced) return
                isClickDebounced = true

                searchHistory.addTrack(track)
                val intent = Intent(this@SearchActivity, AudioPlayer::class.java).apply {
                    val gson = Gson()
                    val trackJson = gson.toJson(track)
                    putExtra(TRACK_EXTRA, trackJson)
                }
                startActivity(intent)

                // Сбрасываем флаг через заданное время
                searchDebounceHandler.postDelayed({
                    isClickDebounced = false
                }, CLICK_DEBOUNCE_DELAY)
            }
        })
    }

    private fun setupSearchHistory() {
        searchHistory = SearchHistory(getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE))
        updateHistoryVisibility()
    }

    private fun setupListeners() {
        // Навигация назад
        findViewById<MaterialToolbar>(R.id.tool_bar_search).setNavigationOnClickListener {
            finish()
        }

        // Слушатель текста в поле поиска с debounce
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                searchText = s?.toString() ?: ""
                updateHistoryVisibility()

                // Удаляем предыдущий отложенный поиск
                searchRunnable?.let { searchDebounceHandler.removeCallbacks(it) }

                // Если текст не пустой, запускаем новый отложенный поиск
                if (!s.isNullOrEmpty()) {
                    searchRunnable = Runnable {
                        performSearch(s.toString())
                    }
                    searchDebounceHandler.postDelayed(searchRunnable!!, SEARCH_DEBOUNCE_DELAY)
                } else {
                    // Если текст пустой, очищаем результаты
                    tracks.clear()
                    adapter.notifyDataSetChanged()
                    placeholderNoFound.visibility = View.GONE
                    placeholderError.visibility = View.GONE
                    progressBar.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Клик по полю поиска - показать клавиатуру
        searchInput.setOnClickListener {
            showKeyBoard()
        }

        // Кнопка очистки поиска
        clearButton.setOnClickListener {
            searchInput.text.clear()
            hideKeyBoard()
            tracks.clear()
            adapter.notifyDataSetChanged()
            placeholderNoFound.visibility = View.GONE
            placeholderError.visibility = View.GONE
            progressBar.visibility = View.GONE
            updateHistoryVisibility()
        }

        // Обработка нажатия Done на клавиатуре
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Отменяем отложенный поиск и выполняем немедленно
                searchRunnable?.let { searchDebounceHandler.removeCallbacks(it) }
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
        progressBar.visibility = View.VISIBLE
        trackList.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val trackDtos = networkClient.searchTracks(query)
                handleSearchResults(ArrayList(trackDtos))
            } catch (e: Exception) {
                handleSearchError()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun handleSearchResults(results: ArrayList<TrackDto>) {
        tracks.clear()
        if (results.isNotEmpty()) {
            // Преобразуем TrackDto в Track перед добавлением
            tracks.addAll(results.map { it.toTrack() })
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
        searchInput.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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

    override fun onDestroy() {
        super.onDestroy()
        // Очищаем все отложенные задачи при уничтожении активности
        searchRunnable?.let { searchDebounceHandler.removeCallbacks(it) }
    }


    companion object {
        const val INPUT_TEXT = "SEARCH_TEXT"
        const val SAVED_TEXT = ""

        // Константы для передачи данных
        const val TRACK_EXTRA = "trackJson"
        const val SHARED_PREFS_NAME = "playlist_maker_prefs"
    }
}