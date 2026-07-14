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
import org.arcade.atomcity.di.apiKeyManagerModule
import org.arcade.atomcity.di.appModule
import org.arcade.atomcity.di.jacketImagesModule
import org.arcade.atomcity.di.network.maiteaNetworkModule
import org.arcade.atomcity.di.network.maiteaProfileDataModule
import org.arcade.atomcity.di.network.taikoNetworkModule
import org.arcade.atomcity.di.viewmodel.maiTeaViewModelModule
import org.arcade.atomcity.di.viewmodel.taikoServerViewModelModule
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.navigation.AppNavigation
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.utils.ApiKeyManager
import org.koin.androidx.viewmodel.ext.android.viewModel
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
                maiteaProfileDataModule,
                maiTeaViewModelModule,
                jacketImagesModule
            )

            val taikoModules = listOf(
                taikoNetworkModule,
                taikoServerViewModelModule
            )

            val utilityModules = listOf(
                apiKeyManagerModule,
                appModule
            )

            modules(maiteaModules + taikoModules + utilityModules)
        }
    }
}

class MainActivity : ComponentActivity() {

    private val maiteaViewModel: MaiteaViewModel by viewModel()
    private val taikoViewModel: TaikoViewModel by viewModel()
    private val apiKeyManager: ApiKeyManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            // Handle permission result if needed
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    AtomCityTheme {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AppNavigation(
                maiteaViewModel = maiteaViewModel,
                apiKeyManager = apiKeyManager,
                taikoViewModel = taikoViewModel
            )
        } else {
            // Fallback for devices below API 26
            Scaffold { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    Text("This feature requires Android 8.0 or higher")
                }
            }
        }
    }
}
