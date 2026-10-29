package com.jahanmusic.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PREFS_NAME = "jahan_prefs"
private val ACCESS_GRANTED = booleanPreferencesKey("access_granted_once")

val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = PREFS_NAME)

/**
 * Remembers that the user granted music access once, so the Access page
 * is never shown again on next launches (system permission is the source
 * of truth; this flag avoids re-prompting flows).
 */
object Prefs {
    fun accessGrantedFlow(context: Context): Flow<Boolean> =
        context.appDataStore.data.map { it[ACCESS_GRANTED] ?: false }

    suspend fun setAccessGranted(context: Context) {
        context.appDataStore.edit { it[ACCESS_GRANTED] = true }
    }
}
