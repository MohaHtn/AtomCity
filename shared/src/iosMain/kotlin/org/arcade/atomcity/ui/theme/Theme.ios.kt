package org.arcade.atomcity.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
actual fun rememberColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    customColor: Color
): ColorScheme {
    // On iOS, we use the custom color provided by the settings
    return if (darkTheme) {
        darkColorScheme(
            primary = customColor,
            secondary = PurpleGrey80,
            tertiary = Pink80
        )
    } else {
        lightColorScheme(
            primary = customColor,
            secondary = PurpleGrey40,
            tertiary = Pink40
        )
    }
}
