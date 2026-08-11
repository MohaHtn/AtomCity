package org.arcade.atomcity.ui.core

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OpenMiniMenu(
    showMiniMenu: Boolean,
    onDismiss: () -> Unit,
    onItemClick: (String) -> Unit,
    extraItems: List<Triple<String, String, String>> = emptyList(),
    modifier: Modifier = Modifier
) {
    val availableKeys by GlobalUIState.availableApiKeys

    val allGames = listOf(
        Triple("maimai", "maimai", "les gros cercles là"),
        Triple("iidx", "beatmania IIDX", "DJ ???? woa"),
        Triple("popn", "pop'n music", "miamme les burgers en forme de boutons"),
        Triple("taiko", "Taiko no Tatsujin", "hit me in the fucking face da-don"),
        Triple("sdvx", "SOUND VOLTEX", "vroum vroum les boutons"),
        Triple("itg", "In The Groove 2", "dance dance revolution but better")
    )

    val filteredGames = allGames.filter { it.first in availableKeys }

    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        if (showMiniMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
            )
        }
        AnimatedVisibility(
            visible = showMiniMenu,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Card(
                modifier = Modifier
                    .width(512.dp)
                    .padding(16.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (filteredGames.isNotEmpty()) {
                        Text(
                            "Jeux",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        filteredGames.forEach { (gameId, headline, supporting) ->
                            ListItem(
                                headlineContent = { Text(headline) },
                                supportingContent = { Text(supporting) },
                                modifier = Modifier.clickable { onItemClick("game/$gameId") }
                            )
                        }
                    }

                    if (extraItems.isNotEmpty()) {
                        if (filteredGames.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                        Text(
                            "Actions",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        extraItems.forEach { (route, headline, supporting) ->
                            ListItem(
                                headlineContent = { Text(headline) },
                                supportingContent = { Text(supporting) },
                                modifier = Modifier.clickable { onItemClick(route) }
                            )
                        }
                    }
                }
            }
        }
    }
}
