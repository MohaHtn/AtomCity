package org.arcade.atomcity.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.arcade.atomcity.presentation.viewmodel.MaimaiViewModel
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.core.WelcomeScreen
import org.arcade.atomcity.ui.core.SettingsScreen
import org.arcade.atomcity.ui.core.GlobalUIState
import org.arcade.atomcity.ui.guide.apistatus.ApiSettingsScreen
import org.arcade.atomcity.ui.game.maimai.GameScreen
import org.arcade.atomcity.ui.game.maimai.MaimaiBest30Charts
import org.arcade.atomcity.ui.game.maimai.MaimaiScoresDetails
import org.arcade.atomcity.ui.game.maimai.AtomCityUsers
import org.arcade.atomcity.ui.game.taiko.TaikoUserSettings
import org.arcade.atomcity.ui.game.taiko.TaikoScoresDetails
import org.arcade.atomcity.ui.game.taiko.TaikoAtomCityUsers
import org.arcade.atomcity.utils.ApiKeyManager
import org.arcade.atomcity.domain.repository.IDifficultyRepository
import org.arcade.atomcity.ui.game.maimai.MaimaiUtageScreen
import org.koin.compose.koinInject

sealed class Screen(val route: String) {
    data object Game : Screen("game/{gameId}") {
        fun createRoute(gameId: String) = "game/$gameId"
    }
    data object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    taikoViewModel: TaikoViewModel,
    maimaiViewModel: MaimaiViewModel,
    apiKeyManager: ApiKeyManager
) {
    val navController = rememberNavController()

    val showMiniMenu: MutableState<Boolean> = remember { mutableStateOf(false) }

    // Use null as initial to wait for DataStore
    val apiChecklistState by apiKeyManager.getApiChecklistStateFlow().collectAsState(initial = null)

    LaunchedEffect(apiChecklistState) {
        apiChecklistState?.let {
            GlobalUIState.availableApiKeys.value = it
        }
    }

    // Wait until we know the API key state before rendering navigation
    val currentApiChecklist = apiChecklistState ?: return

    val snackbarHostState = remember { SnackbarHostState() }
    val taikoSnackbarMessage by taikoViewModel.snackbarMessage.collectAsState()

    LaunchedEffect(taikoSnackbarMessage) {
        taikoSnackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            taikoViewModel.clearSnackbar()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = if (currentApiChecklist.isEmpty()) "welcome" else Screen.Game.createRoute(currentApiChecklist.first()),
            enterTransition = { expandHorizontally() },
            exitTransition = { fadeOut(animationSpec = tween(500)) + slideOutHorizontally() },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
        ) {
            composable(
                Screen.Game.route,
                arguments = listOf(navArgument("gameId") { type = NavType.StringType })
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString("gameId")
                GameScreen(
                    gameId = gameId.toString(),
                    onBackClick = { navController.popBackStack() },
                    maimaiViewModel = maimaiViewModel,
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
                val dataState by maimaiViewModel.data.collectAsState()
                val selectedPlayDetail by maimaiViewModel.selectedPlayDetail.collectAsState()
                
                val scoreEntryFromList = dataState?.data?.find { it.id == scoreId }
                // Prefer the specifically fetched detail if it matches the ID, 
                // as it's more stable than the paginated list which might refresh.
                val scoreEntry = (selectedPlayDetail?.takeIf { it.id == scoreId }) ?: scoreEntryFromList

                LaunchedEffect(scoreId, scoreEntryFromList) {
                    if (scoreEntryFromList == null) {
                        val keyHash = apiKeyManager.getKeyHash("maimai")
                        if (!keyHash.isNullOrBlank()) {
                            maimaiViewModel.getPlayById(scoreId, keyHash)
                        }
                    }
                }

                MaimaiScoresDetails(
                    scoreEntry = scoreEntry,
                    maimaiViewModel = maimaiViewModel,
                    onBackClick = { navController.popBackStack() },
                    onHistoryClick = { historyId ->
                        navController.navigate("maimaiScoresDetails/$historyId")
                    }
                )
            }

            composable (route = "maimaiBest30Scores") {
                val repository: IDifficultyRepository = koinInject()
                MaimaiBest30Charts(
                    navController = navController,
                    onBackClick = { navController.popBackStack() },
                    maimaiViewModel = maimaiViewModel,
                    repository = repository
                )
            }

            composable(route = "maimaiMostPlayed") {
                org.arcade.atomcity.ui.game.maimai.MaimaiMostPlayedChart(
                    onBackClick = { navController.popBackStack() },
                    navController = navController,
                    maimaiViewModel = maimaiViewModel
                )
            }

            composable("maimaiUsers") {
                AtomCityUsers(
                    maimaiViewModel = maimaiViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("taikoUserSettings") {
                TaikoUserSettings(
                    taikoViewModel = taikoViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("taikoUsers") {
                TaikoAtomCityUsers(
                    taikoViewModel = taikoViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = "taikoScoresDetails/{songId}",
                arguments = listOf(navArgument("songId") { type = NavType.IntType })
            ) { backStackEntry ->
                val songId = backStackEntry.arguments?.getInt("songId") ?: 0
                TaikoScoresDetails(
                    songId = songId,
                    taikoViewModel = taikoViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable (
                route = "maimaiUtageScreen",
            ) {
                MaimaiUtageScreen(
                    onBackClick = { navController.popBackStack() },
                    viewModel = maimaiViewModel,
                    navController = navController
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}
