package org.arcade.atomcity.ui.core

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.getValue

@Composable
fun OpenMiniMenu(
    showMiniMenu: Boolean,
    onDismiss: () -> Unit,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val availableKeys by GlobalUIState.availableApiKeys

    val allItems = listOf(
        Triple("maimai", "maimai", "les gros cerles là"),
        Triple("iidx", "beatmania IIDX", "DJ ???? woa"),
        Triple("popn", "pop'n music", "miamme les burgers en forme de boutons"),
        Triple("taiko", "Taiko no Tatsujin", "hit me in the fucking face da-don"),
        Triple("sdvx", "SOUND VOLTEX", "vroum vroum les boutons"),
        Triple("itg", "In The Groove 2", "dance dance revolution but better")
    )

    val filteredItems = allItems.filter { it.first in availableKeys }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = showMiniMenu,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier.width(512.dp).clickable(onClick = onDismiss).padding(16.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    filteredItems.forEach { (_, headline, supporting) ->
                        ListItem(
                            headlineContent = { Text(headline) },
                            supportingContent = { Text(supporting) },
                            modifier = Modifier.clickable { onItemClick(headline) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OpenMiniMenuPreview() {
    Box(
        modifier = Modifier
            .padding(16.dp)
    ) {
        OpenMiniMenu(
            showMiniMenu = true,
            onDismiss = {},
            onItemClick = {},
        )
    }
}