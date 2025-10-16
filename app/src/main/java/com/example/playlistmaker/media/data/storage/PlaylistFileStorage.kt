package com.example.playlistmaker.media.data.storage

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.util.UUID

/**
 * Класс для работы с файловым хранилищем плейлистов
 */
class PlaylistFileStorage(private val context: Context) {

    companion object {
        private const val COVERS_DIRECTORY = "playlist_covers"
    }

    /**
     * Сохраняет изображение обложки из URI в приватное хранилище приложения
     * @param uri URI изображения из галереи
     * @return Путь к сохраненному файлу или null при ошибке
     */
    suspend fun saveCoverImageFromUri(uri: Uri): String? = withContext(Dispatchers.IO) {
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            // Открываем поток из URI
            inputStream = context.contentResolver.openInputStream(uri)

            if (inputStream == null) {
                return@withContext null
            }

            // Создаем директорию для обложек
            val coversDir = File(context.filesDir, COVERS_DIRECTORY)
            if (!coversDir.exists()) {
                coversDir.mkdirs()
            }

            // Генерируем уникальное имя файла
            val fileName = "${UUID.randomUUID()}.jpg"
            val coverFile = File(coversDir, fileName)

            // Копируем содержимое
            outputStream = FileOutputStream(coverFile)
            inputStream.copyTo(outputStream)

            return@withContext coverFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Удаляет файл обложки
     * @param coverPath Путь к файлу обложки
     */
    suspend fun deleteCoverImage(coverPath: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            if (coverPath.isNullOrEmpty()) return@withContext false

            val file = File(coverPath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Проверяет существование файла обложки
     */
    fun coverFileExists(coverPath: String?): Boolean {
        return coverPath?.let { File(it).exists() } ?: false
    }

    /**
     * Получает File объект для обложки
     */
    fun getCoverFile(coverPath: String?): File? {
        return coverPath?.let {
            File(it).takeIf { file -> file.exists() }
        }
    }
}