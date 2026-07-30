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
    val normalizedGameId = gameId.lowercase()
    when {
        normalizedGameId == "maimai" -> {
            MaimaiScores(
                maiteaViewModel = maiteaViewModel,
                navController = navController,
            )
        }
        normalizedGameId == "taiko no tatsujin" || normalizedGameId == "taiko" -> {
            TaikoScores(
                taikoViewModel = taikoViewModel,
                navController = navController
            )
        }
        else -> {
            Log.e("GameScreen", "Unknown gameId: $gameId")
        }
    }
}
