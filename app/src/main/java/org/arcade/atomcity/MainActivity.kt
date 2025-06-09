package org.arcade.atomcity

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.arcade.atomcity.di.apiKeyManagerModule
import org.arcade.atomcity.di.network.maiteaNetworkModule
import org.arcade.atomcity.di.network.taikoNetworkModule
import org.arcade.atomcity.di.viewmodel.maiTeaViewModelModule
import org.arcade.atomcity.di.viewmodel.taikoServerViewModelModule
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.navigation.AppNavigation
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.utils.ApiKeyManager
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AtomCityApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AtomCityApplication)

            val maiteaModules = listOf(
                maiteaNetworkModule,
                maiTeaViewModelModule
            )

            val taikoModules = listOf(
                taikoNetworkModule,
                taikoServerViewModelModule
            )

            val utilityModules = listOf(
                apiKeyManagerModule
            )

            modules(maiteaModules + taikoModules + utilityModules)
        }
    }
}

class MainActivity : ComponentActivity() {

    private val maiteaViewModel: MaiteaViewModel by inject()
    private val taikoViewModel: TaikoViewModel by inject()
    private val apiKeyManager: ApiKeyManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainActivityContent(
                maiteaViewModel = maiteaViewModel,
                apiKeyManager = apiKeyManager,
                taikoViewModel = taikoViewModel
            )
        }
    }
}

@Composable
fun MainActivityContent(maiteaViewModel: MaiteaViewModel, apiKeyManager: ApiKeyManager, taikoViewModel: TaikoViewModel) {
    AtomCityTheme {
        Scaffold { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    AppNavigation(
                        maiteaViewModel = maiteaViewModel,
                        apiKeyManager = apiKeyManager,
                        taikoViewModel = taikoViewModel)
                } else {
                    // Fallback for devices below API 26
                    Text("This feature requires Android 8.0 or higher")
                }
            }
        }
    }
}