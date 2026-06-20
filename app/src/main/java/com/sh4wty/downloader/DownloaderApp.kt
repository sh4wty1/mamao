package com.sh4wty.downloader

import android.app.Application
import android.util.Log
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Initializes the bundled yt-dlp engine + ffmpeg exactly once, off the main thread.
 *
 * [YoutubeDL.init] extracts the Python/yt-dlp payload from the APK on first launch, which is
 * slow, so it must not run on the UI thread. We expose [isReady] so download code can wait for
 * it instead of racing the extraction.
 */
class DownloaderApp : Application() {

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                YoutubeDL.getInstance().init(this@DownloaderApp)
                FFmpeg.getInstance().init(this@DownloaderApp)
                isReady = true
            } catch (e: YoutubeDLException) {
                Log.e(TAG, "yt-dlp engine init failed", e)
            }
        }
    }

    companion object {
        private const val TAG = "DownloaderApp"

        /** True once the engine finished extracting and is safe to call. */
        @Volatile
        var isReady: Boolean = false
            private set
    }
}
