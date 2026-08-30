package org.arcade.atomcity

import androidx.compose.runtime.Composable
import org.arcade.atomcity.presentation.viewmodel.MaimaiViewModel
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.navigation.AppNavigation
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.utils.ApiKeyManager
import org.koin.compose.koinInject
import coil3.compose.setSingletonImageLoaderFactory
import coil3.ImageLoader
import coil3.request.crossfade

import org.arcade.atomcity.utils.addAnimatedDecoders

@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                addAnimatedDecoders()
            }
            .crossfade(true)
            .build()
    }
    AtomCityTheme {
        val maimaiViewModel: MaimaiViewModel = koinInject()
        val taikoViewModel: TaikoViewModel = koinInject()
        val apiKeyManager: ApiKeyManager = koinInject()
        
        AppNavigation(
            taikoViewModel = taikoViewModel,
            maimaiViewModel = maimaiViewModel,
            apiKeyManager = apiKeyManager
        )
    }
}
