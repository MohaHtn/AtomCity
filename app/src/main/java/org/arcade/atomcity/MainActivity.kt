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
import org.arcade.atomcity.di.maiteaNetworkModule
import org.arcade.atomcity.di.viewModelModule
import org.arcade.atomcity.network.MaiteaApiService
import org.arcade.atomcity.presentation.viewmodel.MainActivityViewModel
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
            modules(listOf(maiteaNetworkModule, viewModelModule, apiKeyManagerModule))
        }
    }
}

class MainActivity : ComponentActivity() {
    private val apiService: MaiteaApiService by inject()
    private val mainActivityViewModel: MainActivityViewModel by inject()
    private val apiKeyManager: ApiKeyManager by inject()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request 120Hz refresh rate if device supports it
        if (Build.VERSION.SDK_INT >= 30) { // Build.VERSION_CODES.R is API 30
            window.attributes.preferredDisplayModeId = try {
                val setFrameRateMethod = window.javaClass.getMethod("setFrameRate",
                    Float::class.java, Int::class.java)
                setFrameRateMethod.invoke(window, 120.0f, 1) // 1 = FRAME_RATE_COMPATIBILITY_FIXED_SOURCE
                1
            } catch (e: Exception) {
                0
            }
        }

//        val apiKey = "377|9TdBVuvl96tWpBFezkbCUwZ57aM6gDGAeAjEpMaz"
/*
        val apiKey = "410|V4BVasyDok2kLIBKYVScrMn7Q761jWAPRnOOxcUz"
*/
//        apiKeyManager.saveApiKey("maimai", apiKey)

        setContent {
            MainActivityContent(mainActivityViewModel, apiKeyManager)
        }
    }
}

@Composable
fun MainActivityContent(mainActivityViewModel: MainActivityViewModel, apiKeyManager: ApiKeyManager) {
    AtomCityTheme {
        Scaffold { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    AppNavigation(mainActivityViewModel, apiKeyManager)
                } else {
                    // Fallback for devices below API 26
                    Text("This feature requires Android 8.0 or higher")
                }
            }
        }
    }
}