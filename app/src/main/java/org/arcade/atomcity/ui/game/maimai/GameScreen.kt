package org.arcade.atomcity.ui.game.maimai

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.arcade.atomcity.presentation.viewmodel.MainActivityViewModel
import org.arcade.atomcity.util.ApiKeyManager

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GameScreen(
    gameId: String,
    mainActivityViewModel: MainActivityViewModel,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("api_prefs", Context.MODE_PRIVATE)
    val apiKeyManager = ApiKeyManager(sharedPreferences)

    when {
        gameId == "maimai" && apiKeyManager.hasApiKey("maimai") -> MaimaiScores(mainActivityViewModel)

    }
}