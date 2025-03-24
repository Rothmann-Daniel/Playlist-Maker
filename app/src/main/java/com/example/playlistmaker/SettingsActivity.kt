package com.example.playlistmaker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings) // Установка разметки

        //Обработчик нажатия на кнопку навигаци: Назад
        val navBack = findViewById<MaterialToolbar>(R.id.tool_bar)
        navBack.setNavigationOnClickListener {
            finish()
        }

    }
}