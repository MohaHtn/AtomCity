package org.arcade.atomcity.ui.guide.apistatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.core.GlobalUIState
import org.arcade.atomcity.ui.guide.MaimaiApiGuide
import org.arcade.atomcity.ui.guide.TaikoServerApiGuide
import org.arcade.atomcity.utils.ApiKeyManager
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.koin.compose.koinInject

@Composable
internal fun ApiCheckList() {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ApiItem(
            name = "maimai",
            key = "maimai",
        )
        ApiItem(
            name = "SOUND VOLTEX",
            key = "sdvx",
        )
        ApiItem(
            name = "In The Groove 2",
            key = "itg",
        )
        ApiItem(
            name = "beatmania IIDX",
            key = "iidx",
        )
        ApiItem(
            name = "pop'n music",
            key = "popn",
        )
        ApiItem(
            name = "Taiko no Tatsujin",
            key = "taiko",
        )
    }

    val openApiGuide by GlobalUIState.openApiGuide
    val selectedGame = GlobalUIState.selectedGameForGuide.value

    if (openApiGuide) {
        val apiKeyManager = koinInject<ApiKeyManager>()
        when (selectedGame) {
            "maimai" -> {
                val maiteaViewModel = koinInject<MaiteaViewModel>()
                MaimaiApiGuide(
                    apiKeyManager = apiKeyManager,
                    isVisible = GlobalUIState.openApiGuide,
                    maiteaViewModel = maiteaViewModel
                )
            }
            "Taiko no Tatsujin" -> {
                val taikoViewModel = koinInject<TaikoViewModel>()
                TaikoServerApiGuide(
                    apiKeyManager = apiKeyManager,
                    isVisible = GlobalUIState.openApiGuide,
                    taikoViewModel = taikoViewModel
                )
            }
        }
    }
}

@Preview
@Composable
fun ApiCheckListPreview() {
    AtomCityTheme {
        ApiCheckList()
    }
}
