package org.arcade.atomcity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.arcade.atomcity.di.apiKeyManagerModule
import org.arcade.atomcity.di.appModule
import org.arcade.atomcity.di.network.scorefetcherNetworkModule
import org.arcade.atomcity.di.network.scorefetcherProfileDataModule
import org.arcade.atomcity.di.network.taikoNetworkModule
import org.arcade.atomcity.di.sharedModule
import org.arcade.atomcity.di.viewmodel.scorefetcherViewModelModule
import org.arcade.atomcity.di.viewmodel.taikoServerViewModelModule
import org.arcade.atomcity.di.workerModule
import org.arcade.atomcity.presentation.viewmodel.ScorefetcherViewModel
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.core.GlobalUIState
import org.arcade.atomcity.ui.navigation.AppNavigation
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.utils.ApiKeyManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

import kotlinx.coroutines.flow.first

class AtomCityApplication : Application() {
    private val startupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        GlobalUIState.isMaimaiImportStateReady.value = false
        startKoin {
            androidContext(this@AtomCityApplication)

            val scorefetcherModules = listOf(
                scorefetcherNetworkModule,
                scorefetcherProfileDataModule,
                scorefetcherViewModelModule
            )

            val taikoModules = listOf(
                taikoNetworkModule,
                taikoServerViewModelModule
            )

            val utilityModules = listOf(
                apiKeyManagerModule,
                appModule,
                workerModule
            )

            modules(scorefetcherModules + taikoModules + utilityModules + sharedModule)
        }
        preloadMaimaiImportState()
    }

    private fun preloadMaimaiImportState() {
        startupScope.launch {
            val repository = GlobalContext.get().get<org.arcade.atomcity.data.ScorefetcherRepository>()
            val isImporting = repository.isImportWorkerActive().first()
            GlobalUIState.isImportingMaimaiScores.value = isImporting
            GlobalUIState.isMaimaiImportStateReady.value = true
        }
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            !GlobalUIState.isMaimaiImportStateReady.value
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val requestPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                // Permission handled
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
            App()
        }
    }
}
