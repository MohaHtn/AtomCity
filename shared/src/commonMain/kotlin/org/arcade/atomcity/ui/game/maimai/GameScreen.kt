package org.arcade.atomcity.ui.game.maimai

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import org.arcade.atomcity.presentation.viewmodel.ScorefetcherViewModel
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.game.taiko.TaikoScores
import org.arcade.atomcity.ui.navigation.Screen

@Composable
fun GameScreen(
    gameId: String,
    onBackClick: () -> Unit,
    scorefetcherViewModel: ScorefetcherViewModel,
    taikoViewModel: TaikoViewModel,
    navController: NavHostController
) {
    when (gameId) {
        "maimai" -> MaimaiScores(
            scorefetcherViewModel = scorefetcherViewModel,
            navController = navController
        )
        "taiko" -> TaikoScores(
            taikoViewModel = taikoViewModel,
            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
            onNavigateToRoute = { route -> navController.navigate(route) }
        )
    }
}
