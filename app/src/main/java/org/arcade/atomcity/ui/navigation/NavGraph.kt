package org.arcade.atomcity.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.core.SettingsScreen
import org.arcade.atomcity.ui.core.WelcomeScreen
import org.arcade.atomcity.ui.core.openApiGuide
import org.arcade.atomcity.ui.game.maimai.GameScreen
import org.arcade.atomcity.ui.game.maimai.MaimaiScoresDetails
import org.arcade.atomcity.ui.guide.MaimaiApiGuide
import org.arcade.atomcity.utils.ApiKeyManager

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Game : Screen("game/{gameId}") {
        fun createRoute(gameId: String) = "game/$gameId"
    }
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    taikoViewModel: TaikoViewModel,
    maiteaViewModel: MaiteaViewModel,
    apiKeyManager: ApiKeyManager
) {
    val navController = rememberNavController()

    var showMiniMenu: MutableState<Boolean> = remember { mutableStateOf(false) }
    var lastClickTime: MutableState<Long> = remember { mutableLongStateOf(0L) }
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = { expandHorizontally() },
        exitTransition = { fadeOut(animationSpec = tween(500)) + slideOutHorizontally() },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(700)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(700)) }
    ) {
        composable(Screen.Home.route) {
            val apiChecklistState = apiKeyManager.getApiChecklistState()

            LaunchedEffect(apiChecklistState.value) {
                if (apiChecklistState.value.isEmpty()) {
                    navController.navigate("welcome") {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.Game.createRoute(apiChecklistState.value.first()))
                }
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
                maiteaViewModel = maiteaViewModel,
                taikoViewModel = taikoViewModel,
                navController = navController
            )
        }

        composable("welcome") {
            WelcomeScreen(
                navController = navController,
                apiChecklistState = apiKeyManager.getApiChecklistState()
            )

            // TODO: Ce sera un switch plus tard
            if (openApiGuide.value) {
                MaimaiApiGuide(
                    apiKeyManager = apiKeyManager,
                    isVisible = openApiGuide
                )
            }
        }

        composable(
            route = "maimaiScoresDetails/{scoreId}",
            arguments = listOf(navArgument("scoreId") { type = NavType.IntType })
        ) { backStackEntry ->
            val scoreId = backStackEntry.arguments?.getInt("scoreId") ?: 0
            val dataState = maiteaViewModel.data.collectAsState()
            val scoreEntry = dataState.value?.data?.find { it.id == scoreId }
            MaimaiScoresDetails(
                scoreEntry = scoreEntry
            )
        }

        composable(route = "settings") {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}


