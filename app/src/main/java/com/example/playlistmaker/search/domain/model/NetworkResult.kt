package com.example.playlistmaker.search.domain.model

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Failure(val error: String) : NetworkResult<Nothing>()
}