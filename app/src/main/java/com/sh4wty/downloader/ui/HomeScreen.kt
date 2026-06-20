package com.sh4wty.downloader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.sh4wty.downloader.R
import com.sh4wty.downloader.download.DownloadManager
import com.sh4wty.downloader.download.UpdateResult
import com.sh4wty.downloader.download.Updater
import com.sh4wty.downloader.model.DownloadState
import com.sh4wty.downloader.model.DownloadTask
import com.sh4wty.downloader.model.DownloadType
import kotlinx.coroutines.launch

/**
 * Main screen: a single link field plus the Video/Audio chooser and a live list of downloads.
 * [initialUrl] is prefilled from the clipboard by [com.sh4wty.downloader.MainActivity].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    initialUrl: String,
    onDownload: (String, DownloadType) -> Unit,
) {
    var url by remember { mutableStateOf(initialUrl) }
    val tasks by DownloadManager.tasks.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var updating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(
                        enabled = !updating,
                        onClick = {
                            updating = true
                            scope.launch {
                                val message = when (val result = Updater.update(context)) {
                                    UpdateResult.Updated -> context.getString(R.string.engine_updated)
                                    UpdateResult.UpToDate -> context.getString(R.string.engine_up_to_date)
                                    is UpdateResult.Failed ->
                                        result.message ?: context.getString(R.string.engine_update_failed)
                                }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                updating = false
                            }
                        },
                    ) {
                        if (updating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.update_engine),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.slogan),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp),
            )
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text(stringResource(R.string.paste_link)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            ChooserButtons(
                enabled = url.isNotBlank(),
                onPick = { type -> onDownload(url.trim(), type) },
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(tasks, key = { it.id }) { TaskCard(it) }
            }
        }
    }
}

@Composable
private fun TaskCard(task: DownloadTask) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = task.title ?: task.url,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            when (task.state) {
                DownloadState.QUEUED, DownloadState.RUNNING ->
                    LinearProgressIndicator(
                        progress = { task.progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                DownloadState.COMPLETED ->
                    Text(stringResource(R.string.done), color = MaterialTheme.colorScheme.primary)
                DownloadState.FAILED ->
                    Text(
                        text = task.error ?: stringResource(R.string.download_failed),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
            }
        }
    }
}
