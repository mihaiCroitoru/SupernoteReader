package com.supernote.reader.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("supernote_reader")

class ProgressStore(context: Context) {

    private val store = context.dataStore

    private fun pageKey(path: String) = intPreferencesKey("page:$path")
    private fun totalKey(path: String) = intPreferencesKey("total:$path")

    suspend fun saveProgress(path: String, page: Int, total: Int) {
        store.edit { prefs ->
            prefs[pageKey(path)] = page
            prefs[totalKey(path)] = total
        }
    }

    suspend fun loadProgress(path: String): Pair<Int, Int> {
        val prefs = store.data.first()
        return (prefs[pageKey(path)] ?: 0) to (prefs[totalKey(path)] ?: 0)
    }

    // Load all preferences in one read; use getProgress() to extract per-book values.
    suspend fun loadAllPrefs(): Preferences = store.data.first()
    fun getProgress(prefs: Preferences, path: String): Pair<Int, Int> =
        (prefs[pageKey(path)] ?: 0) to (prefs[totalKey(path)] ?: 0)

    private val fontKey = stringPreferencesKey("font")
    private val sizeKey = intPreferencesKey("font_size")

    val fontFlow: Flow<String> = store.data.map { it[fontKey] ?: "Literata" }
    val sizeFlow: Flow<Int> = store.data.map { it[sizeKey] ?: 16 }

    suspend fun saveFont(name: String) = store.edit { it[fontKey] = name }
    suspend fun saveSize(size: Int) = store.edit { it[sizeKey] = size }

    private fun themeKey(path: String) = stringPreferencesKey("theme:$path")
    fun themeFlow(path: String): Flow<Boolean> = store.data.map { it[themeKey(path)] == "dark" }
    suspend fun saveTheme(path: String, dark: Boolean) = store.edit { it[themeKey(path)] = if (dark) "dark" else "light" }

    private fun lastReadKey(path: String) = longPreferencesKey("lastread:$path")
    fun getLastRead(prefs: Preferences, path: String): Long = prefs[lastReadKey(path)] ?: 0L
    suspend fun saveLastRead(path: String) = store.edit { it[lastReadKey(path)] = System.currentTimeMillis() }
}
