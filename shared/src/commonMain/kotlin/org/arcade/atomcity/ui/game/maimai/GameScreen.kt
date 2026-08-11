package org.arcade.atomcity.ui.game.maimai

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.game.taiko.TaikoScores
import org.arcade.atomcity.ui.navigation.Screen

@Composable
fun GameScreen(
    gameId: String,
    onBackClick: () -> Unit,
    maiteaViewModel: MaiteaViewModel,
    taikoViewModel: TaikoViewModel,
    navController: NavHostController
) {
    when (gameId) {
        "maimai" -> MaimaiScores(
            maiteaViewModel = maiteaViewModel,
            navController = navController
        )
        "taiko" -> TaikoScores(
            taikoViewModel = taikoViewModel,
            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
            onNavigateToRoute = { route -> navController.navigate(route) }
        )
    }
}
