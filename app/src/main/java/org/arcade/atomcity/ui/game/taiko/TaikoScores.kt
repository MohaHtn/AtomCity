package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaikoScores(
    mainActivityViewModel: TaikoViewModel,
    navController: NavHostController
) {

    Text(
        text = "Taiko Scores",
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.padding(12.dp)
    )

}