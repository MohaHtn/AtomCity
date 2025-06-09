package org.arcade.atomcity.utils

import android.content.Context
import android.util.Log
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
     * Synchronous version of saveApiKey for migration compatibility.
     * Consider using the suspend version in new code.
     *
     * @param gameName The name of the game for which the API key is being registered.
     * @param apiKey The API key to register.
     */
    fun saveApiKeyBlocking(gameName: String, apiKey: String) {
        runBlocking {
            saveApiKey(gameName, apiKey)
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
    fun getApiKey(gameName: String): String? {
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
     * Removes the API key for a specific game synchronously.
     * Consider using the suspend version in new code.
     *
     * @param gameName The name of the game for which to remove the API key.
     */
    fun removeApiKeyBlocking(gameName: String) {
        runBlocking {
            removeApiKey(gameName)
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
     * Logs all stored API keys for debugging purposes.
     */
    fun logAllApiKeys() {
        runBlocking {
            try {
                val preferences = context.apiKeysDataStore.data.firstOrNull() ?: return@runBlocking

                Log.d("DataStore", "--- Contenu du DataStore api_keys ---")
                preferences.asMap().forEach { (key, value) ->
                    Log.d("DataStore", "${key.name}: $value")
                }
            } catch (e: Exception) {
                Log.e("DataStore", "Erreur lors de la lecture des clés API", e)
            }
        }
    }
}