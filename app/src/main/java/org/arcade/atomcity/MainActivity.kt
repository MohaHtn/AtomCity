package org.arcade.atomcity

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import org.arcade.atomcity.di.maiteaNetworkModule
import androidx.compose.ui.Modifier
import org.arcade.atomcity.di.viewModelModule
import org.arcade.atomcity.network.MaiteaApiService
import org.arcade.atomcity.presentation.viewmodel.MainActivityViewModel
import org.arcade.atomcity.ui.navigation.AppNavigation
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.util.ApiKeyManager
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AtomCityApplication : Application() {
    override fun onCreate() {
        val apiKey = "377|9TdBVuvl96tWpBFezkbCUwZ57aM6gDGAeAjEpMaz"
        ApiKeyManager.saveApiKey(this, apiKey)

        super.onCreate()
        startKoin {
            androidContext(this@AtomCityApplication)
            modules(listOf(maiteaNetworkModule, viewModelModule))
        }
    }
}

class MainActivity : ComponentActivity() {
    private val apiService: MaiteaApiService by inject()
    private val mainActivityViewModel: MainActivityViewModel by inject()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AtomCityTheme {
                Scaffold { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        AppNavigation(apiService, mainActivityViewModel)
                    }
                }
            }
        }
    }
}