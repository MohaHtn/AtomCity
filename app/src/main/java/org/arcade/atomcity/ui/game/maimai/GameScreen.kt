package org.arcade.atomcity.ui.game.maimai

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.game.taiko.TaikoScores

@Composable
fun GameScreen(
    gameId: String,
    onBackClick: () -> Unit,
    maiteaViewModel: MaiteaViewModel,
    taikoViewModel: TaikoViewModel,
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
        "taiko no tatsujin" -> {
            Log.d("GameScreen", "Navigating to taiko scores screen")
            TaikoScores(
                taikoViewModel = taikoViewModel, // Assuming you have a TaikoViewModel similar to MaiteaViewModel
                navController = navController
            )
        }
        // etc.
    }
}