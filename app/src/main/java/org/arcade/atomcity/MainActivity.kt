package org.arcade.atomcity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.ui.theme.presentation.HomeScreen
import org.arcade.atomcity.util.ApiKeyManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.arcade.atomcity.network.MaiteaApiService
import org.koin.android.ext.android.inject


class MainActivity : ComponentActivity() {
    private val apiService: MaiteaApiService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AtomCityTheme {
                HomeScreen()
                }
            }

            // Ask the user for the API key and save it
            val apiKey = "user_provided_api_key" // Replace with actual user input
            ApiKeyManager.saveApiKey(this, apiKey)

            lifecycleScope.launch {
                val response = apiService.getAllUserScores()
                // Handle the response
            }
        }
}
