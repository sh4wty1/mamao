package com.sh4wty.downloader

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sh4wty.downloader.data.Prefs
import com.sh4wty.downloader.download.DownloadService
import com.sh4wty.downloader.model.DownloadType
import com.sh4wty.downloader.ui.ChooserButtons
import com.sh4wty.downloader.ui.theme.DownloaderTheme
import com.sh4wty.downloader.util.UrlExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The reason this app exists: it appears in the Android share sheet. Another app shares a link
 * here, we extract the URL and float a tiny Video/Audio chooser over the source app. One tap
 * starts the download and dismisses — no full UI.
 */
class ShareReceiverActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = UrlExtractor.firstUrl(sharedText())
        if (url == null) {
            Toast.makeText(this, R.string.no_link_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val prefs = Prefs(applicationContext)
        setContent {
            DownloaderTheme {
                QuickChooser(
                    url = url,
                    onPick = { type ->
                        DownloadService.start(this, url, type)
                        CoroutineScope(Dispatchers.IO).launch { prefs.setLastType(type) }
                        Toast.makeText(this, R.string.download_started, Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onDismiss = ::finish,
                )
            }
        }
    }

    private fun sharedText(): String? =
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            null
        }
}

@Composable
private fun QuickChooser(
    url: String,
    onPick: (DownloadType) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.download)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                ChooserButtons(enabled = true, onPick = onPick)
            }
        },
        confirmButton = {},
    )
}
