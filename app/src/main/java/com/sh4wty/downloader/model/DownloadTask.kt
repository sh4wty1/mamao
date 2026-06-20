package com.sh4wty.downloader.model

/** What the user asked us to grab. */
enum class DownloadType { VIDEO, AUDIO }

enum class DownloadState { QUEUED, RUNNING, COMPLETED, FAILED }

/**
 * A single download as tracked by [com.sh4wty.downloader.download.DownloadManager] and observed
 * by the UI. Immutable; the manager replaces entries to push updates through its StateFlow.
 */
data class DownloadTask(
    val id: String,
    val url: String,
    val type: DownloadType,
    val title: String? = null,
    val progress: Float = 0f,
    val state: DownloadState = DownloadState.QUEUED,
    val error: String? = null,
    val outputUri: String? = null,
)
