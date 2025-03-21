package org.arcade.atomcity.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.arcade.atomcity.presentation.viewmodel.MainActivityViewModel
import org.arcade.atomcity.ui.core.SettingsScreen
import org.arcade.atomcity.ui.core.WelcomeScreen
import org.arcade.atomcity.ui.core.openApiGuide
import org.arcade.atomcity.ui.game.maimai.GameScreen
import org.arcade.atomcity.ui.game.maimai.guide.MaimaiApiGuide
import org.arcade.atomcity.util.ApiKeyManager

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Game : Screen("game/{gameId}") {
        fun createRoute(gameId: String) = "game/$gameId"
    }
    object Settings : Screen("settings")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(mainActivityViewModel: MainActivityViewModel, apiKeyManager: ApiKeyManager) {
    val navController = rememberNavController()

    var showMiniMenu: MutableState<Boolean> = remember { mutableStateOf(false) }
    var lastClickTime: MutableState<Long> = remember { mutableStateOf(0L) }
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            WelcomeScreen()

            if (openApiGuide.value) {
                MaimaiApiGuide(
                    apiKeyManager = apiKeyManager,
                    isVisible = openApiGuide
                )
            }
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

// TODO: fairemarcher  cette merde
//    if(currentRoute != Screen.Home.route) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(bottom = 96.dp)
//        ) {
//            OpenMiniMenu(
//                showMiniMenu = showMiniMenu.value,
//                onDismiss = {
//                    if (System.currentTimeMillis() - lastClickTime.value > 300) {
//                        showMiniMenu.value = !showMiniMenu.value
//                        lastClickTime.value = System.currentTimeMillis()
//                    }
//                },
//                onItemClick = { gameId: String ->
//                    Log.d("AppNavigation", "onItemClick $gameId")
//                    navController.navigate("game/$gameId")
//                    showMiniMenu.value = false
//                },
//                modifier = Modifier.align(Alignment.BottomCenter)
//            )
//
//            Box(modifier = Modifier.offset(y = 800.dp)) {
//                BottomBarPill(
//                    onHomeClick = {
//                        if (System.currentTimeMillis() - lastClickTime.value > 300) {
//                            showMiniMenu.value = !showMiniMenu.value
//                            lastClickTime.value = System.currentTimeMillis()
//                        }
//                    }
//                )
//            }
//        }
    }



