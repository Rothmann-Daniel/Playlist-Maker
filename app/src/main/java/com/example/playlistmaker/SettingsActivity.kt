package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.app.AppCompatDelegate
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