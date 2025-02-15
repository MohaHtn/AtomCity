package org.arcade.atomcity

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.ui.theme.presentation.HomeScreen
import org.arcade.atomcity.util.ApiKeyManager
import kotlinx.coroutines.launch
import org.arcade.atomcity.di.appModule
import org.arcade.atomcity.di.maiteaNetworkModule
import org.arcade.atomcity.di.viewModelModule
import org.arcade.atomcity.network.MaiteaApiService
import org.arcade.atomcity.presentation.viewmodel.MainActivityViewModel
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import retrofit2.HttpException

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AtomCityTheme {
                HomeScreen(mainActivityViewModel)
            }
        }

        lifecycleScope.launch {
            try {
                val response = apiService.getAllUserScores()
                Log.d("MainActivity", "Response: $response")
            } catch (e: HttpException) {
                Log.e("MainActivity", "HTTP error: ${e.code()} ${e.message()}")
            } catch (e: Exception) {
                Log.e("MainActivity", "Unexpected error: ${e.message}")
            }
        }
    }
}