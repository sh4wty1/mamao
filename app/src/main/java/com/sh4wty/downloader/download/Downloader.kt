package com.sh4wty.downloader.download

import android.content.Context
import com.sh4wty.downloader.DownloaderApp
import com.sh4wty.downloader.data.StorageHelper
import com.sh4wty.downloader.model.DownloadType
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

/** Result of a finished download. */
data class DownloadResult(val title: String?, val outputUri: String)

/**
 * Thin wrapper over the yt-dlp engine. Builds the request for video vs audio-only, runs it into a
 * private scratch dir, then publishes the single produced file to Download/Downloader.
 */
object Downloader {

    /**
     * Runs the download, reporting 0..1 progress through [onProgress]. Suspends on IO.
     * Throws on failure (the caller maps it to a FAILED task + user-facing message).
     */
    suspend fun download(
        context: Context,
        taskId: String,
        url: String,
        type: DownloadType,
        onProgress: (Float, String?) -> Unit,
    ): DownloadResult = withContext(Dispatchers.IO) {
        awaitEngine()

        val scratch = StorageHelper.scratchDir(context, taskId)
        scratch.listFiles()?.forEach { it.delete() }

        val request = YoutubeDLRequest(url).apply {
            addOption("-o", "${scratch.absolutePath}/%(title)s.%(ext)s")
            addOption("--no-playlist")
            addOption("--no-mtime")
            when (type) {
                DownloadType.VIDEO -> {
                    // Best video+audio, merged by ffmpeg; falls back to a single combined stream.
                    addOption("-f", "bv*+ba/b")
                    addOption("--merge-output-format", "mp4")
                }
                DownloadType.AUDIO -> {
                    addOption("-x")
                    addOption("--audio-format", "mp3")
                    addOption("--audio-quality", "0")
                }
            }
        }

        var lastTitle: String? = null
        YoutubeDL.getInstance().execute(request, taskId) { progress, _, line ->
            // progress is 0..100 (or -1 before it knows); normalize and surface the status line.
            if (progress >= 0f) onProgress(progress / 100f, line)
            line.substringAfter("[download] Destination: ", "")
                .takeIf { it.isNotBlank() }
                ?.let { lastTitle = File(it).nameWithoutExtension }
        }

        val produced = scratch.listFiles()?.maxByOrNull { it.lastModified() }
            ?: error("Download produced no file")

        val uri = StorageHelper.publishToDownloads(context, produced)
        produced.delete()

        DownloadResult(title = lastTitle ?: produced.nameWithoutExtension, outputUri = uri.toString())
    }

    /** Cancels a running download by its task id (used as the yt-dlp process id). */
    fun cancel(taskId: String) {
        runCatching { YoutubeDL.getInstance().destroyProcessById(taskId) }
    }

    /** Wait for the one-time engine extraction kicked off in [DownloaderApp.onCreate]. */
    internal suspend fun awaitEngine() {
        var waited = 0L
        while (!DownloaderApp.isReady && waited < ENGINE_TIMEOUT_MS) {
            delay(ENGINE_POLL_MS)
            waited += ENGINE_POLL_MS
        }
        check(DownloaderApp.isReady) { "yt-dlp engine is not ready" }
    }

    private const val ENGINE_POLL_MS = 100L
    private const val ENGINE_TIMEOUT_MS = 30_000L
}
