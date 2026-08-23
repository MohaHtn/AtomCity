package org.arcade.atomcity.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesManager(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val SHOW_TAIKO_DASHBOARD_KEY = booleanPreferencesKey("show_taiko_dashboard")
        private val TAIKO_DASHBOARD_HASH_KEY = stringPreferencesKey("taiko_dashboard_hash")
    }

    val showTaikoDashboard: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_TAIKO_DASHBOARD_KEY] ?: true
    }

    val lastTaikoDashboardHash: Flow<String?> = dataStore.data.map { preferences ->
        preferences[TAIKO_DASHBOARD_HASH_KEY]
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
}
