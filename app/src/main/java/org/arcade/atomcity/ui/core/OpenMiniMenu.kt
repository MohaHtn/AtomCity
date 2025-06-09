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

@Composable
fun OpenMiniMenu(
    showMiniMenu: Boolean,
    onDismiss: () -> Unit,
    onItemClick: (String) -> Unit,
    items: List<Pair<String, String>> = listOf(
        "maimai" to "les gros cerles là",
        "beatmania IIDX" to "DJ ???? woa",
        "pop n' music" to "miamme les burgers en forme de boutons",
        "taiko no tatsujin" to "hit me in the fucking face da-don",
    ),
    modifier: Modifier = Modifier
) {
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
                    items.forEach { (headline, supporting) ->
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