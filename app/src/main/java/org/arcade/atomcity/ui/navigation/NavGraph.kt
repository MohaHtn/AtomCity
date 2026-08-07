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
import org.arcade.atomcity.ui.core.SettingsScreen
import org.arcade.atomcity.ui.core.WelcomeScreen
import org.arcade.atomcity.ui.core.GlobalUIState
import org.arcade.atomcity.ui.guide.TaikoServerApiGuide
import org.arcade.atomcity.ui.core.settings.ApiSettings
import org.arcade.atomcity.ui.game.maimai.AtomCityUsers
import org.arcade.atomcity.ui.game.maimai.GameScreen
import org.arcade.atomcity.ui.game.maimai.MaimaiBest30Charts
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

    val showMiniMenu: MutableState<Boolean> = remember { mutableStateOf(false) }

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

        composable("welcome") {
            WelcomeScreen(
                navController = navController,
                apiKeyManager = apiKeyManager,
                onContinueClick = { firstGameId ->
                    navController.navigate(Screen.Game.createRoute(firstGameId))
                }
            )

            // TODO: Ce sera un switch plus tard
            if (GlobalUIState.openApiGuide.value) {
                when (GlobalUIState.selectedGameForGuide.value) {
                    "Taiko no Tatsujin" -> {
                        TaikoServerApiGuide(
                            apiKeyManager = apiKeyManager,
                            isVisible = GlobalUIState.openApiGuide,
                            taikoViewModel = taikoViewModel
                        )
                    }
                    else -> {
                        MaimaiApiGuide(
                            apiKeyManager = apiKeyManager,
                            isVisible = GlobalUIState.openApiGuide,
                            maiteaViewModel = maiteaViewModel
                        )
                    }
                }
            }
        }

        composable(
            route = "maimaiScoresDetails/{scoreId}",
            arguments = listOf(navArgument("scoreId") { type = NavType.IntType })
        ) { backStackEntry ->
            val scoreId = backStackEntry.arguments?.getInt("scoreId") ?: 0
            val dataState by maiteaViewModel.data.collectAsState()
            val selectedPlayDetail by maiteaViewModel.selectedPlayDetail.collectAsState()
            
            // Search in main list first
            val scoreEntryFromList = dataState?.data?.find { it.id == scoreId }
            
            // Use the one from list if found, otherwise use the specifically fetched one
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

        composable(route = "settings") {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                navController = navController,
            )
        }

        composable(route = "apiSettings") {
            ApiSettings(
                onBackClick = { navController.popBackStack() },
                apiKeyManager = apiKeyManager,
                maiteaViewModel = maiteaViewModel,
                taikoViewModel = taikoViewModel
            )
        }

        composable (route = "maimaiUsers"){
            AtomCityUsers(
                maiteaViewModel = maiteaViewModel,
                onBackClick = { navController.popBackStack() },
                navController = navController,
                showMiniMenu = showMiniMenu,
                taikoViewModel = taikoViewModel
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

    }
}
