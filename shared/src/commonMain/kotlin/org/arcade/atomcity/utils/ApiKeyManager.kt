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
            preferences.asMap().keys.mapNotNull { key ->
                val name = key.name
                when {
                    name == "taiko_access_code" -> "taiko"
                    name.startsWith("taiko_") -> null // Filter out other taiko internal keys
                    else -> name
                }
            }.distinct()
        }
    }

    suspend fun saveApiKey(gameName: String, apiKey: String) {
        val key = stringPreferencesKey(gameName)
        dataStore.edit { preferences ->
            preferences[key] = apiKey.trim()
        }
    }

    suspend fun saveTaikoCredentials(accessCode: String, password: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("taiko_access_code")] = accessCode.trim()
            preferences[stringPreferencesKey("taiko_password")] = password.trim()
        }
    }

    suspend fun saveTaikoAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("taiko_auth_token")] = token
        }
    }

    fun getTaikoAccessCodeFlow(): Flow<String?> = dataStore.data.map { it[stringPreferencesKey("taiko_access_code")] }
    fun getTaikoPasswordFlow(): Flow<String?> = dataStore.data.map { it[stringPreferencesKey("taiko_password")] }
    fun getTaikoAuthTokenFlow(): Flow<String?> = dataStore.data.map { it[stringPreferencesKey("taiko_auth_token")] }

    suspend fun getTaikoAccessCode(): String? = getTaikoAccessCodeFlow().firstOrNull()
    suspend fun getTaikoPassword(): String? = getTaikoPasswordFlow().firstOrNull()
    suspend fun getTaikoAuthToken(): String? = getTaikoAuthTokenFlow().firstOrNull()

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

    suspend fun removeTaikoCredentials() {
        dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey("taiko_access_code"))
            preferences.remove(stringPreferencesKey("taiko_password"))
            preferences.remove(stringPreferencesKey("taiko_auth_token"))
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
            preferences.asMap().keys.mapNotNull { key ->
                val name = key.name
                when {
                    name == "taiko_access_code" -> "taiko"
                    name.startsWith("taiko_") -> null
                    else -> name
                }
            }.distinct()
        } catch (e: Exception) {
            PlatformUtils.log("ApiKeyManager", "Erreur lors de la lecture des clés API: ${e.message}", true)
            emptyList()
        }
    }
}
