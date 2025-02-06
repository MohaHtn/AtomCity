package org.arcade.atomcity.util

import android.content.Context
import android.content.SharedPreferences

object ApiKeyManager {
    private const val PREFS_NAME = "api_prefs"
    private const val KEY_API_KEY = "maitea_api_key"

    fun saveApiKey(context: Context, apiKey: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_API_KEY, apiKey)
            apply()
        }
    }

    fun getApiKey(context: Context): String? {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_API_KEY, null)
    }
}