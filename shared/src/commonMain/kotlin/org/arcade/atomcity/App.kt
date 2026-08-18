package org.arcade.atomcity

import androidx.compose.runtime.Composable
import org.arcade.atomcity.presentation.viewmodel.ScorefetcherViewModel
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.navigation.AppNavigation
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.utils.ApiKeyManager
import org.koin.compose.koinInject

@Composable
fun App() {
    AtomCityTheme {
        val scorefetcherViewModel: ScorefetcherViewModel = koinInject()
        val taikoViewModel: TaikoViewModel = koinInject()
        val apiKeyManager: ApiKeyManager = koinInject()
        
        AppNavigation(
            taikoViewModel = taikoViewModel,
            scorefetcherViewModel = scorefetcherViewModel,
            apiKeyManager = apiKeyManager
        )
    }
}
