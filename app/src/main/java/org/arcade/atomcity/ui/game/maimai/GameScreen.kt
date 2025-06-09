package org.arcade.atomcity.ui.game.maimai

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import org.arcade.atomcity.presentation.viewmodel.MainActivityViewModel
import org.arcade.atomcity.utils.ApiKeyManager

@Composable
fun GameScreen(
    gameId: String,
    mainActivityViewModel: MainActivityViewModel,
    navController: NavHostController,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val apiKeyManager = ApiKeyManager(context)

    when {
        gameId?.equals("maimai") == true && apiKeyManager.hasApiKey("maimai") -> MaimaiScores(mainActivityViewModel, navController)
    }

    // Example usage of onBackClick
    // Add a button or gesture to trigger onBackClick
}