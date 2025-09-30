package com.example.playlistmaker.media.data.db.di

import androidx.room.Room
import com.example.playlistmaker.media.data.db.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "playlist_maker_database"
        ).build()
    }
}