package com.sh4wty.downloader.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sh4wty.downloader.R
import com.sh4wty.downloader.model.DownloadType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * Foreground service that runs downloads so they survive the app going to the background, and
 * surfaces progress in a notification. One coroutine per enqueued download; the service stops
 * itself once nothing is active.
 */
class DownloadService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val active = AtomicInteger(0)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra(EXTRA_URL)
        val type = intent?.getStringExtra(EXTRA_TYPE)?.let { DownloadType.valueOf(it) }
        if (url.isNullOrBlank() || type == null) {
            stopIfIdle()
            return START_NOT_STICKY
        }

        val taskId = DownloadManager.create(url, type)
        active.incrementAndGet()
        startForeground(NOTIF_ID, buildNotification(getString(R.string.starting), 0, true))

        scope.launch {
            DownloadManager.markRunning(taskId)
            runCatching {
                Downloader.download(this@DownloadService, taskId, url, type) { progress, _ ->
                    DownloadManager.updateProgress(taskId, progress, null)
                    notify(buildNotification(typeLabel(type), (progress * 100).toInt(), false))
                }
            }.onSuccess { result ->
                DownloadManager.markCompleted(taskId, result.title, result.outputUri)
                showDone(result.title ?: typeLabel(type), success = true)
            }.onFailure { e ->
                Log.w(TAG, "download failed for $url", e) // keep the raw yt-dlp output for debugging
                val friendly = getString(DownloadErrors.messageRes(e.message))
                DownloadManager.markFailed(taskId, friendly)
                showDone(friendly, success = false)
            }
            if (active.decrementAndGet() == 0) stopIfIdle()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun stopIfIdle() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun typeLabel(type: DownloadType): String = getString(
        if (type == DownloadType.AUDIO) R.string.downloading_audio else R.string.downloading_video,
    )

    private fun buildNotification(text: String, progress: Int, indeterminate: Boolean) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_download)
            .setOngoing(true)
            .setProgress(100, progress, indeterminate)
            .setOnlyAlertOnce(true)
            .build()

    private fun notify(notification: android.app.Notification) {
        if (hasNotifPermission()) NotificationManagerCompat.from(this).notify(NOTIF_ID, notification)
    }

    private fun showDone(text: String, success: Boolean) {
        if (!hasNotifPermission()) return
        val n = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(if (success) R.string.done else R.string.download_failed))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_download)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(this).notify(DONE_NOTIF_BASE + active.get(), n)
    }

    private fun hasNotifPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.channel_downloads),
            NotificationManager.IMPORTANCE_LOW,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "DownloadService"
        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_TYPE = "extra_type"
        private const val CHANNEL_ID = "downloads"
        private const val NOTIF_ID = 1
        private const val DONE_NOTIF_BASE = 1000

        /** Enqueue a download; starts the foreground service. */
        fun start(context: Context, url: String, type: DownloadType) {
            val intent = Intent(context, DownloadService::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TYPE, type.name)
            }
            context.startForegroundService(intent)
        }
    }
}
