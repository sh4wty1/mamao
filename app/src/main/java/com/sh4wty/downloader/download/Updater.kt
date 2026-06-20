package com.sh4wty.downloader.download

import android.content.Context
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Outcome of a runtime yt-dlp update, mapped to a user message by the UI. */
sealed interface UpdateResult {
    data object Updated : UpdateResult
    data object UpToDate : UpdateResult
    data class Failed(val message: String?) : UpdateResult
}

/**
 * Updates the bundled yt-dlp binary at runtime. This is how breakages get fixed when sites
 * (TikTok/Instagram/YouTube) change — independent of shipping a new app version. Uses the
 * NIGHTLY channel because it ships fixes fastest.
 */
object Updater {

    suspend fun update(context: Context): UpdateResult = withContext(Dispatchers.IO) {
        runCatching {
            Downloader.awaitEngine()
            val status = YoutubeDL.getInstance()
                .updateYoutubeDL(context, YoutubeDL.UpdateChannel.NIGHTLY)
            when (status) {
                YoutubeDL.UpdateStatus.ALREADY_UP_TO_DATE -> UpdateResult.UpToDate
                else -> UpdateResult.Updated // DONE or null
            }
        }.getOrElse { UpdateResult.Failed(it.message) }
    }
}
