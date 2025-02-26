package org.arcade.atomcity.ui.game.maimai

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import org.arcade.atomcity.presentation.viewmodel.MainActivityViewModel



@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("NotConstructor")
@Composable
fun GameScreen(gameId: String, mainActivityViewModel: MainActivityViewModel, onBackClick: () -> Unit) {
    when (gameId) {
        "maimai" -> MaimaiScores(mainActivityViewModel)
        else -> {}
    }
}
