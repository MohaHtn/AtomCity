package org.arcade.atomcity.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import platform.UIKit.UIApplication
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent

@Composable
actual fun PlatformThemeSetup(darkTheme: Boolean) {
    val darkStatusBarIcons = StatusBarAppearance.darkIconsOverride ?: !darkTheme
    SideEffect {
        // The app is hosted in SwiftUI, which ignores the status bar style of nested view
        // controllers, so it is set app-wide (requires UIViewControllerBasedStatusBarAppearance = NO).
        @Suppress("DEPRECATION")
        UIApplication.sharedApplication.setStatusBarStyle(
            if (darkStatusBarIcons) UIStatusBarStyleDarkContent else UIStatusBarStyleLightContent,
            animated = true
        )
    }
}

@Composable
actual fun rememberColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    customColor: Color,
    isAmoledMode: Boolean
): ColorScheme {
    val baseScheme = if (darkTheme) {
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

    return if (darkTheme && isAmoledMode) {
        baseScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceDim = Color.Black,
            surfaceBright = Color(0xFF121212),
            surfaceContainerLowest = Color.Black,
            surfaceContainerLow = Color(0xFF0A0A0A),
            surfaceContainer = Color(0xFF121212),
            surfaceContainerHigh = Color(0xFF1A1A1A),
            surfaceContainerHighest = Color(0xFF222222),
            surfaceVariant = Color(0xFF161616)
        )
    } else {
        baseScheme
    }
}
