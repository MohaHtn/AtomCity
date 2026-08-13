package org.arcade.atomcity.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class ApiKeyManager(private val dataStore: DataStore<Preferences>) {

    fun getApiChecklistStateFlow(): Flow<List<String>> {
        return dataStore.data.map { preferences ->
            preferences.asMap().mapNotNull { (key, value) ->
                if (value is String) key.name else null
            }
        }
    }

    suspend fun saveApiKey(gameName: String, apiKey: String) {
        val key = stringPreferencesKey(gameName)
        dataStore.edit { preferences ->
            preferences[key] = apiKey.trim()
        }
    }

    fun getApiKeyFlow(gameName: String): Flow<String?> {
        val key = stringPreferencesKey(gameName)
        return dataStore.data.map { preferences ->
            preferences[key]
        }
    }

    suspend fun getApiKey(gameName: String?): String? {
        if (gameName == null) return null
        val key = stringPreferencesKey(gameName)
        return dataStore.data.map { preferences ->
            preferences[key]
        }.firstOrNull()
    }

    suspend fun removeApiKey(gameName: String) {
        val key = stringPreferencesKey(gameName)
        dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }

    suspend fun hasApiKey(gameName: String): Boolean {
        return getApiKey(gameName) != null
    }

    suspend fun getKeyHash(game: String?): String? {
        val apiKey = getApiKey(game)?.trim() ?: return null
        return PlatformUtils.sha256(apiKey)
    }

    suspend fun getAvailableApiKeys(): List<String> {
        return try {
            val preferences = dataStore.data.firstOrNull() ?: return emptyList()
            preferences.asMap().mapNotNull { (key, value) ->
                if (value is String) key.name else null
            }
        } catch (e: Exception) {
            PlatformUtils.log("ApiKeyManager", "Erreur lors de la lecture des clés API: ${e.message}", true)
            emptyList()
        }
    }
}
