package com.example.playlistmaker.media.data.db.di


import com.example.playlistmaker.media.data.db.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single<AppDatabase> {
        AppDatabase.getInstance(androidContext())
    }
}