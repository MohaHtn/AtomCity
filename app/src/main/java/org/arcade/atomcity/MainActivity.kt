package org.arcade.atomcity

import android.Manifest
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.arcade.atomcity.di.appModule
import org.arcade.atomcity.di.sharedModule
import org.arcade.atomcity.ui.core.GlobalUIState
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

            modules(appModule + sharedModule)
        }
        preloadMaimaiImportState()
    }

    private fun preloadMaimaiImportState() {
        startupScope.launch {
            val repository = GlobalContext.get().get<org.arcade.atomcity.domain.repository.IScorefetcherRepository>()
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
