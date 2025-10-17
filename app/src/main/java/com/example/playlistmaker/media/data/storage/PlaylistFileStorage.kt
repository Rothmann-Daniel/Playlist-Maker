package com.example.playlistmaker.media.data.storage

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
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
        private const val DEFAULT_EXTENSION = ".jpg"
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

            // Получаем MIME тип и расширение файла
            val fileExtension = getFileExtensionFromUri(uri) ?: DEFAULT_EXTENSION

            // Создаем директорию для обложек
            val coversDir = File(context.filesDir, COVERS_DIRECTORY)
            if (!coversDir.exists()) {
                coversDir.mkdirs()
            }

            // Генерируем уникальное имя файла с правильным расширением
            val fileName = "${UUID.randomUUID()}$fileExtension"
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
     * Получает расширение файла из URI на основе MIME типа
     * @param uri URI файла
     * @return Расширение файла с точкой (например: ".jpg", ".png") или null если не удалось определить
     */
    private fun getFileExtensionFromUri(uri: Uri): String? {
        return try {
            // Способ 1: Получаем MIME тип через ContentResolver
            val mimeType = context.contentResolver.getType(uri)

            // Если MIME тип получен, конвертируем в расширение
            mimeType?.let { type ->
                when (type) {
                    "image/png" -> ".png"
                    "image/jpeg", "image/jpg" -> ".jpg"
                    "image/webp" -> ".webp"
                    "image/gif" -> ".gif"
                    "image/bmp" -> ".bmp"
                    else -> getExtensionFromMimeType(type) ?: DEFAULT_EXTENSION
                }
            } ?: run {
                // Способ 2: Если MIME тип не получен, пытаемся извлечь из URI
                getExtensionFromUriPath(uri) ?: DEFAULT_EXTENSION
            }
        } catch (e: Exception) {
            e.printStackTrace()
            DEFAULT_EXTENSION
        }
    }

    /**
     * Конвертирует MIME тип в расширение файла
     * @param mimeType MIME тип (например: "image/jpeg")
     * @return Расширение файла с точкой или null
     */
    private fun getExtensionFromMimeType(mimeType: String): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?.let { ".$it" }
    }

    /**
     * Пытается извлечь расширение из пути URI
     * @param uri URI файла
     * @return Расширение файла с точкой или null
     */
    private fun getExtensionFromUriPath(uri: Uri): String? {
        return try {
            val path = uri.toString()
            val lastDotIndex = path.lastIndexOf('.')
            if (lastDotIndex != -1 && lastDotIndex < path.length - 1) {
                val extension = path.substring(lastDotIndex).lowercase()
                // Проверяем, что расширение валидное
                if (extension in setOf(".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp")) {
                    extension
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Проверяет, является ли MIME тип поддерживаемым изображением
     */
    private fun isValidImageMimeType(mimeType: String?): Boolean {
        return mimeType?.startsWith("image/") == true && mimeType in setOf(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/bmp"
        )
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

    /**
     * Получает MIME тип файла по его пути
     */
    fun getMimeTypeFromPath(filePath: String?): String? {
        return filePath?.let { path ->
            val extension = path.substringAfterLast('.', "").lowercase()
            when (extension) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "webp" -> "image/webp"
                "gif" -> "image/gif"
                "bmp" -> "image/bmp"
                else -> null
            }
        }
    }
}