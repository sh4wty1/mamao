package com.sh4wty.downloader.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File

/**
 * Moves a freshly-downloaded file into the public Download/Downloader folder via MediaStore.
 *
 * yt-dlp writes into our app-private scratch dir (no permission needed); we then publish the
 * result through MediaStore so it survives uninstall and is visible to the user / other apps.
 * Using MediaStore means we need no WRITE_EXTERNAL_STORAGE permission on Android 10+.
 */
object StorageHelper {

    private const val SUBDIR = "Downloader"

    /** App-private scratch directory yt-dlp downloads into before we publish it. */
    fun scratchDir(context: Context, taskId: String): File {
        val base = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir
        return File(base, "tmp/$taskId").apply { mkdirs() }
    }

    /**
     * Publishes [file] into Download/Downloader and returns the resulting content Uri.
     * Best-effort MIME guess from the extension; falls back to a generic type.
     */
    fun publishToDownloads(context: Context, file: File): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, file.name)
            put(MediaStore.Downloads.MIME_TYPE, mimeOf(file.extension))
            put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$SUBDIR")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val uri = resolver.insert(collection, values)
            ?: error("MediaStore refused to create an entry for ${file.name}")

        resolver.openOutputStream(uri).use { out ->
            requireNotNull(out) { "Could not open output stream for $uri" }
            file.inputStream().use { it.copyTo(out) }
        }

        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return uri
    }

    private fun mimeOf(ext: String): String = when (ext.lowercase()) {
        "mp4", "m4v" -> "video/mp4"
        "webm" -> "video/webm"
        "mkv" -> "video/x-matroska"
        "mp3" -> "audio/mpeg"
        "m4a" -> "audio/mp4"
        "opus", "ogg" -> "audio/ogg"
        else -> "application/octet-stream"
    }
}
