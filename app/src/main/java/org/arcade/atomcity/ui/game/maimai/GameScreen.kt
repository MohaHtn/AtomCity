package org.arcade.atomcity.ui.game.maimai

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel

@Composable
fun GameScreen(
    gameId: String,
    onBackClick: () -> Unit,
    maiteaViewModel: MaiteaViewModel,
    navController: NavHostController
) {
    when (gameId) {
        "maimai" -> {
            Log.d("GameScreen", "Navigating to maimai scores screen")
            MaimaiScores(
                maiteaViewModel = maiteaViewModel,
                navController = navController
            )
        }
        "taiko" -> {
            // Utiliser uniquement taikoScoresViewModel ici
            // Ne pas référencer mainActivityViewModel
        }
        // etc.
    }
}