package org.arcade.atomcity.util

import android.content.SharedPreferences

class ApiKeyManager(private val sharedPreferences: SharedPreferences) {
    
    fun saveApiKey(gameName: String, apiKey: String) {
        with(sharedPreferences.edit()) {
            putString(gameName, apiKey)
            apply()
        }
    }

    fun getApiKey(gameName: String): String? {
        return sharedPreferences.getString(gameName, null)
    }

    fun removeApiKey(gameName: String) {
        with(sharedPreferences.edit()) {
            remove(gameName)
            apply()
        }
    }

    fun hasApiKey(gameName: String): Boolean {
        return sharedPreferences.contains(gameName)
    }
}