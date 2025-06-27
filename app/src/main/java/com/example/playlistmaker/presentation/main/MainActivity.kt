package com.example.playlistmaker.presentation.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.media.MediaActivity
import com.example.playlistmaker.presentation.search.SearchActivity

import com.example.playlistmaker.presentation.settings.SettingsActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Переход на экран поиска
        findViewById<Button>(R.id.search_button).setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        // Переход на экран медиатеки
        findViewById<Button>(R.id.media_button).setOnClickListener {
            val intent = Intent(this, MediaActivity::class.java)
            startActivity(intent)
        }

        // Переход на экран настроек
        findViewById<Button>(R.id.settings_button).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

}

