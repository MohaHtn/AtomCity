package org.arcade.atomcity.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesManager(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val SHOW_TAIKO_DASHBOARD_KEY = booleanPreferencesKey("show_taiko_dashboard")
        private val TAIKO_DASHBOARD_HASH_KEY = stringPreferencesKey("taiko_dashboard_hash")
        private val FAVORITE_SONGS_KEY = stringSetPreferencesKey("taiko_favorite_songs")
    }

    val showTaikoDashboard: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_TAIKO_DASHBOARD_KEY] ?: true
    }

    val lastTaikoDashboardHash: Flow<String?> = dataStore.data.map { preferences ->
        preferences[TAIKO_DASHBOARD_HASH_KEY]
    }

    val favoriteSongIds: Flow<Set<Int>> = dataStore.data.map { preferences ->
        preferences[FAVORITE_SONGS_KEY]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }

    suspend fun setShowTaikoDashboard(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_TAIKO_DASHBOARD_KEY] = show
        }
    }

    suspend fun setTaikoDashboardHash(hash: String) {
        dataStore.edit { preferences ->
            preferences[TAIKO_DASHBOARD_HASH_KEY] = hash
        }
    }

    suspend fun toggleFavoriteSong(songId: Int) {
        dataStore.edit { preferences ->
            val current = preferences[FAVORITE_SONGS_KEY] ?: emptySet()
            val songIdStr = songId.toString()
            if (current.contains(songIdStr)) {
                preferences[FAVORITE_SONGS_KEY] = current - songIdStr
            } else {
                preferences[FAVORITE_SONGS_KEY] = current + songIdStr
            }
        }
    }
}
