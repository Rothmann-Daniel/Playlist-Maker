package com.example.playlistmaker

import android.os.Bundle
import android.view.RoundedCorner
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners


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

        val imageUrl = "https://img.freepik.com/premium-vector/draw-cat-brown-box-so-funny-word-have-nice-day_45130-561.jpg?w=740"

        val image = findViewById<ImageView>(R.id.imageTestInternet)
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.splash)
            .transform(RoundedCorners(100))
            .into(image)
        //Glide.with(applicationContext).load(imageUrl).into(image)
    }

}

