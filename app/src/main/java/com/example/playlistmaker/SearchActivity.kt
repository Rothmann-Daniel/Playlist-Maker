package com.example.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class SearchActivity : AppCompatActivity() {

    private var inputText: String = DEF_TEXT //для хранения текста из поисковой строки
    private lateinit var searchInput: EditText // Объявление (без инициализации) поля для поисковой строки (EditText)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)


        //Обработчик нажатия на кнопку навигаци: Назад
        val navBack = findViewById<MaterialToolbar>(R.id.tool_bar)
        navBack.setNavigationOnClickListener {
            finish()
        }

        //Инициализация полей: поисковой строки и кнопки очистки
        searchInput = findViewById<EditText>(R.id.searchInput)
        val searchInputClear = findViewById<ImageView>(R.id.searchInputClear)

        // Обработчик клика по кнопке очистки: очищает текст в поисковой строке и скрывает клавиатуру
        searchInputClear.setOnClickListener {
            searchInput.setText("")
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(searchInput.windowToken, 0)
        }

        //Создание объекта TextWatcher для отслеживания изменений текста. beforeTextChanged не используется.
        val searchInputTextWatcher = object : TextWatcher {

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchInputClear.visibility = inputClearVisibility(s)
                inputText = searchInput.text.toString()
            }

            //При изменении текста обновляется видимость кнопки очистки и сохраняется текущий текст.
            override fun afterTextChanged(p0: Editable?) {}

        }
        //Установка TextWatcher для поисковой строки
        searchInput.addTextChangedListener(searchInputTextWatcher)

    }

    // Метод для  определения видимости кнопки очистки
    private fun inputClearVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
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