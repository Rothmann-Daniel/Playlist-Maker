package com.example.playlistmaker

import android.content.Context
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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.model.Track
import com.example.playlistmaker.model.TrackResponse
import com.example.playlistmaker.network.iTunesAPI
import com.example.playlistmaker.trackAdapter.TrackAdapter
import com.google.android.material.appbar.MaterialToolbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {
    private val trackBaseURL = "https://itunes.apple.com"

    private val retrofit = Retrofit.Builder()
        .baseUrl(trackBaseURL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val trackService = retrofit.create(iTunesAPI::class.java)

    private lateinit var searchInput: EditText
    private lateinit var clearButton: ImageView
    private var searchText: String = SAVED_TEXT  // переменная для хранения введённого текста
    private var lastInput: String? = null       // переменная для хранения последнего введенного текста

    // константы для сохранения и извлечения данных
    companion object {
        const val INPUT_TEXT = "SEARCH_TEXT"
        const val SAVED_TEXT = ""
    }

    private lateinit var placeholderNoFound: LinearLayout
    private lateinit var placeholderError: LinearLayout
    private lateinit var updateButton: Button
    private lateinit var trackList: RecyclerView

    private val tracks = ArrayList<Track>()
    private val adapter = TrackAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        //val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.tool_bar_search)
        searchInput = findViewById(R.id.searchInput)
        clearButton = findViewById<ImageView>(R.id.button_clear)
        updateButton = findViewById(R.id.button_update)
        placeholderNoFound = findViewById(R.id.notFound_placeholder)
        placeholderError = findViewById(R.id.error_placeholder)
        trackList = findViewById(R.id.recyclerTrackList)

        adapter.tracks = tracks
        trackList.adapter = adapter

        //Обработчик нажатия на кнопку навигаци: Назад
        val navBack = findViewById<MaterialToolbar>(R.id.tool_bar_search)
        navBack.setNavigationOnClickListener {
            finish()
        }

        // создаём анонимный класс TextWatcher для обработки ввода текста
        val searchTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (s.isNullOrEmpty()) {
                    clearButton.visibility =
                        View.GONE
                } else {
                    clearButton.visibility = View.VISIBLE
                    searchText = s.toString()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        searchInput.addTextChangedListener(searchTextWatcher)

        // по клику на строку поиска выводим клавиатуру
        searchInput.setOnClickListener() {
            showKeyBoard()
        }

        // обрабатываем нажатие на кнопку очистить
        clearButton.setOnClickListener() {
            searchInput.text.clear()
            clearButton.visibility = View.GONE
            hideKeyBoard()
            tracks.clear()
            adapter.notifyDataSetChanged()
            placeholderNoFound.visibility = View.GONE
        }

        // получаем сохраненное значение
        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(INPUT_TEXT, SAVED_TEXT)
            searchInput.setText(searchText)
        }

        //обрабатываем нажатие на кнопку Done на клавиатуре
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val input = searchInput.text.toString()
                lastInput = input
                trackSearch(input)
                true
            }
            false
        }

        // по нажатию на кнопку обновить - выполняем последний сохраненный поисковый API запрос
        updateButton.setOnClickListener {
            val input = searchInput.text.toString()
            lastInput = input
            lastInput?.let { input ->
                trackSearch(input)
            }
        }
    }

    private fun showKeyBoard() {
        searchInput.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyBoard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(searchInput.windowToken, 0)
    }

    // сохраняем  введенное значение
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INPUT_TEXT, searchText)
    }

    // метод для поискового запроса
    private fun trackSearch(input: String) {
        placeholderNoFound.visibility = View.GONE
        placeholderError.visibility = View.GONE
        trackList.visibility = View.VISIBLE

        trackService.search(input)
            .enqueue(object : Callback<TrackResponse> {
                override fun onResponse(
                    call: Call<TrackResponse>,
                    response: Response<TrackResponse>
                ) {
                    if (response.isSuccessful) {
                        tracks.clear()
                        val results = response.body()?.results

                        if (!results.isNullOrEmpty()) {
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
                    } else {
                        tracks.clear()
                        adapter.notifyDataSetChanged()
                        placeholderNoFound.visibility = View.GONE
                        placeholderError.visibility = View.VISIBLE
                        trackList.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                    tracks.clear()
                    adapter.notifyDataSetChanged()
                    placeholderNoFound.visibility = View.GONE
                    placeholderError.visibility = View.VISIBLE
                    trackList.visibility = View.GONE
                }
            })
    }
}