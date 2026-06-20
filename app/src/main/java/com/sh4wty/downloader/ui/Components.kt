package com.sh4wty.downloader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sh4wty.downloader.R
import com.sh4wty.downloader.model.DownloadType

/**
 * The Video / Audio chooser. Shared by the main screen and the share-sheet popup so the two
 * entry points feel identical.
 */
@Composable
fun ChooserButtons(
    enabled: Boolean,
    onPick: (DownloadType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = { onPick(DownloadType.VIDEO) },
            enabled = enabled,
            modifier = Modifier.weight(1f).height(56.dp),
        ) {
            Icon(Icons.Default.Videocam, contentDescription = null)
            Text(text = stringResLabel(R.string.video), modifier = Modifier.padding(start = 8.dp))
        }
        FilledTonalButton(
            onClick = { onPick(DownloadType.AUDIO) },
            enabled = enabled,
            modifier = Modifier.weight(1f).height(56.dp),
        ) {
            Icon(Icons.Default.MusicNote, contentDescription = null)
            Text(text = stringResLabel(R.string.audio), modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun stringResLabel(resId: Int): String =
    androidx.compose.ui.res.stringResource(resId)
