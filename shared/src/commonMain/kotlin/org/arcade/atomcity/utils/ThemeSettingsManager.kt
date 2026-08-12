package org.arcade.atomcity.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemeSettingsManager(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val THEME_COLOR_KEY = intPreferencesKey("theme_color")
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

    suspend fun setThemeColor(color: Color) {
        dataStore.edit { preferences ->
            preferences[THEME_COLOR_KEY] = color.toArgb()
        }
    }
}
