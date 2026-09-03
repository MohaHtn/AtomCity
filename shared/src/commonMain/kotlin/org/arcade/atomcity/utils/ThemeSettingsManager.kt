package org.arcade.atomcity.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

class ThemeSettingsManager(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val THEME_COLOR_KEY = intPreferencesKey("theme_color")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val AMOLED_MODE_KEY = booleanPreferencesKey("amoled_mode")

        val DEFAULT_COLOR = Color(0xFF6650a4) // Purple40
        
        val PREDEFINED_COLORS = listOf(
            Color(0xFF6650a4), // Purple
            Color(0xFF006494), // Blue
            Color(0xFF006D3B), // Green
            Color(0xFFBF360C), // Red/Orange
            Color(0xFF4A6572), // Blue Grey
            Color(0xFF000000), // Black
            Color(0xFFC2185B), // Pink
            Color(0xFF7B1FA2), // Deep Purple
        )
    }

    val themeColor: Flow<Color> = dataStore.data.map { preferences ->
        val argb = preferences[THEME_COLOR_KEY]
        if (argb != null) Color(argb) else DEFAULT_COLOR
    }

    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val modeStr = preferences[THEME_MODE_KEY]
        if (modeStr != null) {
            try {
                ThemeMode.valueOf(modeStr)
            } catch (_: Exception) {
                ThemeMode.SYSTEM
            }
        } else {
            ThemeMode.SYSTEM
        }
    }

    val isAmoledMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AMOLED_MODE_KEY] ?: false
    }

    suspend fun setThemeColor(color: Color) {
        dataStore.edit { preferences ->
            preferences[THEME_COLOR_KEY] = color.toArgb()
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    suspend fun setAmoledMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AMOLED_MODE_KEY] = enabled
        }
    }
}
