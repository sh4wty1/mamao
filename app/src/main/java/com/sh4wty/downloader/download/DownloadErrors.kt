package com.sh4wty.downloader.download

import androidx.annotation.StringRes
import com.sh4wty.downloader.R

/**
 * Maps the raw yt-dlp failure output to a short, friendly message. yt-dlp dumps a wall of
 * warnings + a stack-tracey ERROR line; we never show that to the user — we classify it into one
 * of a few human messages, pointing at the refresh/update button when an update would likely fix it.
 */
object DownloadErrors {

    // Signals that the engine is outdated / an extractor broke — almost always fixed by updating.
    private val UPDATE_HINTS = listOf(
        "older than",
        "yt-dlp --update",
        "yt-dlp -u",
        "unable to extract",
        "confirm you are on the latest version",
        "update to the latest version",
        "nsig extraction failed",
        "unable to download webpage",
        "failed to extract",
    )

    private val NETWORK_HINTS = listOf(
        "unable to connect",
        "connection reset",
        "timed out",
        "timeout",
        "network is unreachable",
        "name or service not known",
        "temporary failure in name resolution",
    )

    private val UNSUPPORTED_HINTS = listOf(
        "unsupported url",
        "is not a valid url",
        "private video",
        "this video is private",
        "login required",
        "requested format is not available",
        "no video formats found",
    )

    @StringRes
    fun messageRes(raw: String?): Int {
        val text = raw?.lowercase() ?: return R.string.error_generic
        return when {
            // Network first: a connection failure can also say "unable to download webpage".
            NETWORK_HINTS.any { it in text } -> R.string.error_network
            UNSUPPORTED_HINTS.any { it in text } -> R.string.error_unsupported
            UPDATE_HINTS.any { it in text } -> R.string.error_outdated
            else -> R.string.error_generic
        }
    }
}
