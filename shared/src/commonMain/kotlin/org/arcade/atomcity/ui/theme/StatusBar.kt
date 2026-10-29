package org.arcade.atomcity.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Status bar icon color requested by the current screen, applied by [PlatformThemeSetup].
 * null means the icons follow the app theme.
 */
object StatusBarAppearance {
    var darkIconsOverride by mutableStateOf<Boolean?>(null)
        internal set
}

/**
 * Forces dark (or light) status bar icons while this effect is in the composition,
 * e.g. for a screen drawing an image behind the status bar.
 */
@Composable
fun StatusBarIconsEffect(darkIcons: Boolean) {
    DisposableEffect(darkIcons) {
        StatusBarAppearance.darkIconsOverride = darkIcons
        onDispose { StatusBarAppearance.darkIconsOverride = null }
    }
}
