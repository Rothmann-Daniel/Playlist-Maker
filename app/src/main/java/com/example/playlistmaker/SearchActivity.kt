package com.example.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.track.Track
import com.example.playlistmaker.track.TrackAdapter
import com.google.android.material.appbar.MaterialToolbar

class SearchActivity : AppCompatActivity() {

    private var inputText: String = DEF_TEXT //для хранения текста из поисковой строки
    private lateinit var searchInput: EditText // поле для поисковой строки (EditText)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)


        //Обработчик нажатия на кнопку навигаци: Назад
        val navBack = findViewById<MaterialToolbar>(R.id.tool_bar_search)
        navBack.setNavigationOnClickListener {
            finish()
        }

        //Инициализация полей: поисковой строки и кнопки очистки
        searchInput = findViewById<EditText>(R.id.searchInput)
        val searchInputClear = findViewById<ImageView>(R.id.searchInputClear)

        // Обработчик клика по кнопке очистки: очищает текст в поисковой строке и скрывает клавиатуру
        searchInputClear.setOnClickListener {
            searchInput.setText("")
            searchInput.requestFocus()
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(searchInput.windowToken, 0)
        }

        //Создание объекта TextWatcher для отслеживания изменений текста.
        val searchInputTextWatcher = object : TextWatcher {

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchInputClear.isVisible =
                    !s.isNullOrEmpty() // При изменении текста обновляется видимость кнопки очистки
                inputText = searchInput.text.toString()
            }

            //При изменении текста обновляется видимость кнопки очистки и сохраняется текущий текст.
            override fun afterTextChanged(p0: Editable?) {}

        }
        //Установка TextWatcher для поисковой строки
        searchInput.addTextChangedListener(searchInputTextWatcher)

        // Track: RecyclerView, TrackList: List<Track>

        val trackAdapter = TrackAdapter(
            listOf(
                Track(
                    "Smells Like Teen Spirit",
                    "Nirvana",
                    "5:01",
                    "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg"
                ),
                Track(
                    "Billie Jean",
                    "Michael Jackson",
                    "4:35",
                    "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg"
                ),
                Track(
                    "Stayin' Alive",
                    "Bee Gees",
                    "4:10",
                    "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg"
                ),
                Track(
                    "Whole Lotta Love",
                    "Led Zeppelin",
                    "5:33",
                    "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg"
                ),
                Track(
                    "Sweet Child O'Mine",
                    "Guns N' Roses",
                    "5:03",
                    "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg "
                )
            )
        )

        val trackRecycler: RecyclerView = findViewById(R.id.recyclerTrackView)
        trackRecycler.adapter = trackAdapter

    }


    // Сохранение состояния: текста из поисковой строки перед уничтожением активности
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INPUT_TEXT, inputText)
        Log.d(TAG, "В onSaveInstanceState сохранен текст: $inputText")
    }


    // Восстановление состояния: текста поисковой строки при повторном создании активности
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        inputText = savedInstanceState.getString(INPUT_TEXT, DEF_TEXT)
        searchInput.setText(inputText)
        Log.d(TAG, "В onRestoreInstanceState восстановлен текст: $inputText")
    }

    companion object {
        private const val INPUT_TEXT = "INPUT_TEXT" // ключ для сохранения состояния
        private const val DEF_TEXT = "" // текст по умолчанию
        private const val TAG = "SEARCH_TEST" //тег для логов
    }


}



