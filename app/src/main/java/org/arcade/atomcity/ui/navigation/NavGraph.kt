package org.arcade.atomcity.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.arcade.atomcity.network.MaiteaApiService
import org.arcade.atomcity.presentation.viewmodel.MainActivityViewModel
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
fun AppNavigation(apiService: MaiteaApiService, mainActivityViewModel: MainActivityViewModel) {
    val navController = rememberNavController()

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
}