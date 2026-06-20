package com.sh4wty.downloader.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sh4wty.downloader.model.DownloadType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "prefs")

/** Tiny persisted preferences. Currently just the last chosen download type. */
class Prefs(private val context: Context) {

    val lastType: Flow<DownloadType> = context.dataStore.data.map { prefs ->
        prefs[LAST_TYPE]?.let { runCatching { DownloadType.valueOf(it) }.getOrNull() }
            ?: DownloadType.VIDEO
    }

    suspend fun setLastType(type: DownloadType) {
        context.dataStore.edit { it[LAST_TYPE] = type.name }
    }

    private companion object {
        val LAST_TYPE = stringPreferencesKey("last_type")
    }
}
