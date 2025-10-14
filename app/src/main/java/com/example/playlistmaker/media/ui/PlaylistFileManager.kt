package com.example.playlistmaker.media.ui

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.util.UUID

class PlaylistFileManager(private val context: Context) {

    companion object {
        private const val COVERS_DIRECTORY = "playlist_covers"
    }

    fun getInputStreamFromUri(uri: Uri): InputStream? {
        return try {
            context.contentResolver.openInputStream(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveCoverImage(inputStream: InputStream): String? = withContext(Dispatchers.IO) {
        var outputStream: FileOutputStream? = null
        try {
            // Создаем директорию, если она не существует
            val coversDir = File(context.filesDir, COVERS_DIRECTORY)
            if (!coversDir.exists()) {
                coversDir.mkdirs()
            }

            // Создаем уникальное имя файла
            val fileName = "${UUID.randomUUID()}.jpg"
            val coverFile = File(coversDir, fileName)

            // Копируем файл
            outputStream = FileOutputStream(coverFile)
            inputStream.copyTo(outputStream)

            return@withContext coverFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try {
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun getCoverFile(coverPath: String?): File? {
        return coverPath?.let { File(it) }?.takeIf { it.exists() }
    }
}