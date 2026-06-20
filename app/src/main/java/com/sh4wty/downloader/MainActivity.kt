package com.sh4wty.downloader

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.sh4wty.downloader.data.Prefs
import com.sh4wty.downloader.download.DownloadService
import com.sh4wty.downloader.model.DownloadType
import com.sh4wty.downloader.ui.HomeScreen
import com.sh4wty.downloader.ui.theme.DownloaderTheme
import com.sh4wty.downloader.util.UrlExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Main UI entry point: paste a link, pick Video/Audio, watch progress. */
class MainActivity : ComponentActivity() {

    private val notifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* best-effort */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()

        val prefilled = UrlExtractor.firstUrl(clipboardText()) ?: ""
        val prefs = Prefs(applicationContext)

        setContent {
            DownloaderTheme {
                HomeScreen(
                    initialUrl = prefilled,
                    onDownload = { url, type -> startDownload(url, type, prefs) },
                )
            }
        }
    }

    private fun startDownload(url: String, type: DownloadType, prefs: Prefs) {
        DownloadService.start(this, url, type)
        CoroutineScope(Dispatchers.IO).launch { prefs.setLastType(type) }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun clipboardText(): String? {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        return clipboard?.primaryClip?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)?.coerceToText(this)?.toString()
    }
}
