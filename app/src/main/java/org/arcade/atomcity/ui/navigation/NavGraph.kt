package org.arcade.atomcity.ui.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.arcade.atomcity.presentation.viewmodel.MainActivityViewModel
import org.arcade.atomcity.ui.core.BottomBarPill
import org.arcade.atomcity.ui.core.OpenMiniMenu
import org.arcade.atomcity.ui.core.SettingsScreen
import org.arcade.atomcity.ui.core.WelcomeScreen
import org.arcade.atomcity.ui.game.maimai.GameScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Game : Screen("game/{gameId}") {
        fun createRoute(gameId: String) = "game/$gameId"
    }
    object Settings : Screen("settings")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(mainActivityViewModel: MainActivityViewModel) {
    val navController = rememberNavController()

    var showMiniMenu: MutableState<Boolean> = remember { mutableStateOf(false) }
    var lastClickTime: MutableState<Long> = remember { mutableStateOf(0L) }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            WelcomeScreen()
        }

        composable(
            Screen.Game.route,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")
            GameScreen(
                gameId = gameId.toString(),
                onBackClick = { navController.popBackStack() },
                mainActivityViewModel = mainActivityViewModel
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 96.dp)
    ) {
        // MiniMenu
        OpenMiniMenu(
            showMiniMenu = showMiniMenu.value,
            onDismiss = {
                if (System.currentTimeMillis() - lastClickTime.value > 300) {
                    showMiniMenu.value = !showMiniMenu.value
                    lastClickTime.value = System.currentTimeMillis()
                }
            },
            onItemClick = { gameId: String ->
                Log.d("AppNavigation", "onItemClick $gameId")
                navController.navigate("game/$gameId")
                showMiniMenu.value = false
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // BottomBarPill always at bottom
    Box(modifier = Modifier.offset(y = 800.dp)) {
        BottomBarPill(
            onHomeClick = {
                if (System.currentTimeMillis() - lastClickTime.value > 300) {
                    showMiniMenu.value = !showMiniMenu.value
                    lastClickTime.value = System.currentTimeMillis()
                }
            }
        )
    }
}