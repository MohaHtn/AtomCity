package org.arcade.atomcity.ui.game.maimai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.data.remote.model.scorefetcher.playerDetailsResponse.Icon
import org.arcade.atomcity.data.remote.model.scorefetcher.playerDetailsResponse.Options
import org.arcade.atomcity.data.remote.model.scorefetcher.playerDetailsResponse.PlayerDetailsData
import org.arcade.atomcity.data.remote.model.scorefetcher.playerDetailsResponse.Title

@Preview(showBackground = true)
@Composable
fun PreviewMaimaiPlayerDetailsExpanded() {
    MaterialTheme {
        MaimaiPlayerDetailsContent(
            playerData = PlayerDetailsData(
                name = "MohaHtn",
                rating = 1543,
                options = Options(
                    icon = Icon(png = "https://example.com/icon.png"),
                    title = Title(
                        id = 1,
                        value = "Weekend Dancer"
                    )
                )
            ),
            collapsedFraction = 0f
        )
    }
}

@Preview
@Composable
fun PreviewMaimaiPlayerDetailsCollapsed() {
    MaterialTheme {
        Box(modifier = Modifier.background(Color.DarkGray).padding(8.dp)) {
            MaimaiPlayerDetailsContent(
                playerData = PlayerDetailsData(
                    name = "MohaHtn",
                    rating = 1543,
                    options = Options(
                        icon = Icon(png = "https://example.com/icon.png"),
                        title = Title(
                            id = 1,
                            value = "Weekend Dancer"
                        )
                    )
                ),
                collapsedFraction = 1f
            )
        }
    }
}
