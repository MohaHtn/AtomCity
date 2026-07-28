package org.arcade.atomcity.utils

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * ApiKeyManager class to manage API keys stored in DataStore.
 *
 * This class replaces the previous SharedPreferences implementation with
 * Jetpack DataStore for better type safety and asynchronous API access.
 *
 * @param context The application context used to access DataStore.
 */
class ApiKeyManager(private val context: Context) {
    // Define a companion object for the DataStore instance
    companion object {
        private val Context.apiKeysDataStore: DataStore<Preferences> by preferencesDataStore(name = "api_keys")
    }

    /**
     * Provides a Flow of the list of games having an API key (persistent state).
    */
    fun getApiChecklistStateFlow(): Flow<List<String>> {
        return context.apiKeysDataStore.data.map { preferences ->
            preferences.asMap().mapNotNull { (key, value) ->
                if (value is String) key.name else null
            }
        }
    }

    /**
     * Provides a MutableState<List<String>> initialized from the DataStore (for Compose usage).
     * Note: it will not be automatically updated if the DataStore changes.
     * Prefer getApiChecklistStateFlow() for responsiveness.
     */
    fun getApiChecklistState(): MutableState<List<String>> {
        val state = mutableStateOf<List<String>>(emptyList())
        runBlocking {
            val keys = getAvailableApiKeys()
            state.value = keys
        }
        return state
    }

    /**
     * Saves an API key for a specific game.
     *
     * @param gameName The name of the game for which the API key is being registered.
     * @param apiKey The API key to register.
     */
    suspend fun saveApiKey(gameName: String, apiKey: String) {
        val key = stringPreferencesKey(gameName)
        context.apiKeysDataStore.edit { preferences ->
            preferences[key] = apiKey
        }
    }

    /**
     * Gets the API key flow for a specific game.
     *
     * @param gameName The name of the game for which to retrieve the API key.
     * @return A Flow of the registered API key, or null if no key is found.
     */
    fun getApiKeyFlow(gameName: String): Flow<String?> {
        val key = stringPreferencesKey(gameName)
        return context.apiKeysDataStore.data.map { preferences ->
            preferences[key]
        }
    }

    /**
     * Gets the API key for a specific game synchronously.
     * This is a blocking call and should be used carefully.
     *
     * @param gameName The name of the game for which to retrieve the API key.
     * @return The registered API key, or null if no key is found.
     */
    fun getApiKey(gameName: String?): String? {
        if (gameName == null) return null
        val key = stringPreferencesKey(gameName)
        return runBlocking {
            context.apiKeysDataStore.data.map { preferences ->
                preferences[key]
            }.firstOrNull()
        }
    }

    /**
     * Removes the API key for a specific game.
     *
     * @param gameName The name of the game for which to remove the API key.
     */
    suspend fun removeApiKey(gameName: String) {
        val key = stringPreferencesKey(gameName)
        context.apiKeysDataStore.edit { preferences ->
            preferences.remove(key)
        }
    }


    /**
     * Checks if an API key exists for a specific game.
     *
     * @param gameName The name of the game for which to check the existence of an API key.
     * @return true if an API key exists and is not null, false otherwise.
     */
    fun hasApiKey(gameName: String): Boolean {
        return getApiKey(gameName) != null
    }

    /**
     * Returns the SHA-256 hash of the 'maimai' API key.
     */
    fun getKeyHash(game: String?): String? {
        val apiKey = getApiKey(game) ?: return null
        return sha256Hex(apiKey)
    }

    private fun sha256Hex(text: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            val v = b.toInt() and 0xff
            if (v < 16) sb.append('0')
            sb.append(Integer.toHexString(v))
        }
        return sb.toString()
    }


    /**
     * Gets a list of all available API keys.
     *
     * @return A list of game names for which API keys are registered.
     */
    fun getAvailableApiKeys(): List<String> {
        val keys = mutableListOf<String>()
        runBlocking {
            try {
                val preferences = context.apiKeysDataStore.data.firstOrNull() ?: return@runBlocking

                preferences.asMap().forEach { (key, value) ->
                    if (value is String) {
                        keys.add(key.name)
                    }
                }
            } catch (e: Exception) {
                Log.e("DataStore", "Erreur lors de la lecture des clés API", e)
            }
        }
        return keys
    }

}