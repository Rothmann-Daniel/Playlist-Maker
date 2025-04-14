package com.example.playlistmaker

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar


class MediaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_media)

        //Обработчик нажатия на кнопку навигаци: Назад
        val navBack = findViewById<MaterialToolbar>(R.id.tool_bar_media)
        navBack.setNavigationOnClickListener {
            finish()
        }
    }

}

