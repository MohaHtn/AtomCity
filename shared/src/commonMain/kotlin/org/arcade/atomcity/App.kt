package org.arcade.atomcity

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.crossfade
import org.arcade.atomcity.presentation.viewmodel.MaimaiViewModel
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.navigation.AppNavigation
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.utils.ApiKeyManager
import org.arcade.atomcity.utils.addAnimatedDecoders
import org.koin.compose.koinInject

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
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
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
}
