package org.arcade.atomcity.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.core.WelcomeScreen
import org.arcade.atomcity.ui.core.SettingsScreen
import org.arcade.atomcity.ui.core.GlobalUIState
import org.arcade.atomcity.ui.guide.apistatus.ApiSettingsScreen
import org.arcade.atomcity.ui.game.maimai.GameScreen
import org.arcade.atomcity.ui.game.maimai.MaimaiBest30Charts
import org.arcade.atomcity.ui.game.maimai.MaimaiScoresDetails
import org.arcade.atomcity.ui.game.maimai.AtomCityUsers
import org.arcade.atomcity.utils.ApiKeyManager

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Game : Screen("game/{gameId}") {
        fun createRoute(gameId: String) = "game/$gameId"
    }
    data object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    taikoViewModel: TaikoViewModel,
    maiteaViewModel: MaiteaViewModel,
    apiKeyManager: ApiKeyManager
) {
    val navController = rememberNavController()

    val showMiniMenu: MutableState<Boolean> = remember { mutableStateOf(false) }

    val apiChecklistState by apiKeyManager.getApiChecklistStateFlow().collectAsState(initial = emptyList())

    LaunchedEffect(apiChecklistState) {
        GlobalUIState.availableApiKeys.value = apiChecklistState
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = { expandHorizontally() },
        exitTransition = { fadeOut(animationSpec = tween(500)) + slideOutHorizontally() },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
    ) {
        composable(Screen.Home.route) {
            val apiChecklistState by apiKeyManager.getApiChecklistStateFlow().collectAsState(initial = emptyList())

            LaunchedEffect(apiChecklistState) {
                if (apiChecklistState.isEmpty()) {
                    navController.navigate("welcome") {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.Game.createRoute(apiChecklistState.first()))
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

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                navController = navController
            )
        }

        composable("apiSettings") {
            ApiSettingsScreen(onBackClick = { navController.popBackStack() })
        }

        composable("welcome") {
            WelcomeScreen(
                onContinueClick = { firstGameId ->
                    navController.navigate(Screen.Game.createRoute(firstGameId))
                }
            )
        }

        composable(
            route = "maimaiScoresDetails/{scoreId}",
            arguments = listOf(navArgument("scoreId") { type = NavType.IntType })
        ) { backStackEntry ->
            val scoreId = backStackEntry.arguments?.getInt("scoreId") ?: 0
            val dataState by maiteaViewModel.data.collectAsState()
            val selectedPlayDetail by maiteaViewModel.selectedPlayDetail.collectAsState()
            
            val scoreEntryFromList = dataState?.data?.find { it.id == scoreId }
            val scoreEntry = scoreEntryFromList ?: selectedPlayDetail?.takeIf { it.id == scoreId }

            LaunchedEffect(scoreId, scoreEntryFromList) {
                if (scoreEntryFromList == null) {
                    val keyHash = apiKeyManager.getKeyHash("maimai")
                    if (!keyHash.isNullOrBlank()) {
                        maiteaViewModel.getPlayById(scoreId, keyHash)
                    }
                }
            }

            DisposableEffect(scoreId) {
                onDispose {
                    maiteaViewModel.clearSelectedPlayDetail()
                }
            }

            MaimaiScoresDetails(
                scoreEntry = scoreEntry,
                maiteaViewModel = maiteaViewModel,
                onBackClick = { navController.popBackStack() },
                onHistoryClick = { historyId ->
                    navController.navigate("maimaiScoresDetails/$historyId")
                }
            )
        }

        composable (route = "maimaiBest30Scores") {
            MaimaiBest30Charts(
                navController = navController,
                onBackClick = { navController.popBackStack() },
                maiteaViewModel = maiteaViewModel
            )
        }

        composable(route = "maimaiMostPlayed") {
            org.arcade.atomcity.ui.game.maimai.MaimaiMostPlayedChart(
                onBackClick = { navController.popBackStack() },
                navController = navController,
                maiteaViewModel = maiteaViewModel
            )
        }

        composable("maimaiUsers") {
            AtomCityUsers(
                maiteaViewModel = maiteaViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
