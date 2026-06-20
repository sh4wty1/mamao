package com.sh4wty.downloader.download

import com.sh4wty.downloader.model.DownloadState
import com.sh4wty.downloader.model.DownloadTask
import com.sh4wty.downloader.model.DownloadType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Single source of truth for download state observed by the UI. The work itself runs in
 * [DownloadService]; this object only holds and mutates the task list.
 */
object DownloadManager {

    private val _tasks = MutableStateFlow<List<DownloadTask>>(emptyList())
    val tasks: StateFlow<List<DownloadTask>> = _tasks.asStateFlow()

    /** Creates a QUEUED task and returns its id; the service picks it up to run. */
    fun create(url: String, type: DownloadType): String {
        val task = DownloadTask(id = UUID.randomUUID().toString(), url = url, type = type)
        _tasks.update { listOf(task) + it }
        return task.id
    }

    fun markRunning(id: String) = patch(id) { it.copy(state = DownloadState.RUNNING) }

    fun updateProgress(id: String, progress: Float, title: String?) = patch(id) {
        it.copy(progress = progress, title = title ?: it.title, state = DownloadState.RUNNING)
    }

    fun markCompleted(id: String, title: String?, outputUri: String) = patch(id) {
        it.copy(
            state = DownloadState.COMPLETED,
            progress = 1f,
            title = title ?: it.title,
            outputUri = outputUri,
        )
    }

    fun markFailed(id: String, error: String) = patch(id) {
        it.copy(state = DownloadState.FAILED, error = error)
    }

    fun clearFinished() = _tasks.update { list ->
        list.filter { it.state == DownloadState.RUNNING || it.state == DownloadState.QUEUED }
    }

    private inline fun patch(id: String, transform: (DownloadTask) -> DownloadTask) {
        _tasks.update { list -> list.map { if (it.id == id) transform(it) else it } }
    }
}
