package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun NameplatePreviewContainer(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(Color(0xFF222222))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            color = Color.LightGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .size(width = 332.dp, height = 80.dp)
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Preview(showBackground = true, name = "Expanded Mode (Default)")
@Composable
fun PreviewTaikoNameplateExpanded() {
    MaterialTheme {
        NameplatePreviewContainer(title = "Expanded Mode (collapsedFraction = 0f)") {
            TaikoNameplate(
                playerName = "ドンちゃん",
                title = "太鼓の達人",
                nameplateUrls = emptyList(),
                collapsedFraction = 0f,
                modifier = Modifier.size(width = 332.dp, height = 80.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Compact / Collapsed Mode")
@Composable
fun PreviewTaikoNameplateCollapsed() {
    MaterialTheme {
        NameplatePreviewContainer(title = "Compact Mode (collapsedFraction = 1f)") {
            TaikoNameplate(
                playerName = "ドンちゃん",
                title = "名人",
                nameplateUrls = emptyList(),
                collapsedFraction = 1f,
                modifier = Modifier.size(width = 332.dp, height = 80.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Transition Comparison (0f, 0.5f, 1f)")
@Composable
fun PreviewTaikoNameplateTransition() {
    MaterialTheme {
        Surface(color = Color(0xFF1E1E1E)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Collapsed Fraction: 0.0 (Expanded)", color = Color.White, fontSize = 12.sp)
                Box(modifier = Modifier.size(332.dp, 80.dp)) {
                    TaikoNameplate(
                        playerName = "MohaHtn",
                        title = "Weekend Master",
                        nameplateUrls = emptyList(),
                        collapsedFraction = 0f,
                        modifier = Modifier.size(332.dp, 80.dp)
                    )
                }

                Text("Collapsed Fraction: 0.5 (Intermediate)", color = Color.White, fontSize = 12.sp)
                Box(modifier = Modifier.size(332.dp, 80.dp)) {
                    TaikoNameplate(
                        playerName = "MohaHtn",
                        title = "Weekend Master",
                        nameplateUrls = emptyList(),
                        collapsedFraction = 0.5f,
                        modifier = Modifier.size(332.dp, 80.dp)
                    )
                }

                Text("Collapsed Fraction: 1.0 (Compact)", color = Color.White, fontSize = 12.sp)
                Box(modifier = Modifier.size(332.dp, 80.dp)) {
                    TaikoNameplate(
                        playerName = "MohaHtn",
                        title = "Weekend Master",
                        nameplateUrls = emptyList(),
                        collapsedFraction = 1f,
                        modifier = Modifier.size(332.dp, 80.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Long Text & AutoResize")
@Composable
fun PreviewTaikoNameplateLongText() {
    MaterialTheme {
        NameplatePreviewContainer(title = "Long Player Name & Title") {
            TaikoNameplate(
                playerName = "SuperUltraTaikoPlayer",
                title = "全良達成者・太鼓神・達人",
                nameplateUrls = emptyList(),
                collapsedFraction = 0f,
                modifier = Modifier.size(width = 332.dp, height = 80.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Without Title")
@Composable
fun PreviewTaikoNameplateNoTitle() {
    MaterialTheme {
        NameplatePreviewContainer(title = "No Title (playerName only)") {
            TaikoNameplate(
                playerName = "PlayerOne",
                title = null,
                nameplateUrls = emptyList(),
                collapsedFraction = 0f,
                modifier = Modifier.size(width = 332.dp, height = 80.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Narrow Screen Mode")
@Composable
fun PreviewTaikoNameplateNarrow() {
    MaterialTheme {
        NameplatePreviewContainer(title = "Narrow Screen (isNarrow = true, collapsed = 1f)") {
            TaikoNameplate(
                playerName = "どんちゃん",
                title = "初級者",
                nameplateUrls = emptyList(),
                collapsedFraction = 1f,
                isNarrow = true,
                modifier = Modifier.size(width = 240.dp, height = 60.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Custom Offsets & Font Sizes")
@Composable
fun PreviewTaikoNameplateCustomOffsets() {
    MaterialTheme {
        NameplatePreviewContainer(title = "Custom Offsets & Font Sizes") {
            TaikoNameplate(
                playerName = "TaikoMaster",
                title = "Custom Title",
                nameplateUrls = emptyList(),
                collapsedFraction = 1f,
                titleOffsetX = 2.dp,
                titleOffsetY = (-1).dp,
                nameOffsetX = 0.dp,
                nameOffsetY = 1.dp,
                titleFontSize = 12.sp,
                nameFontSize = 16.sp,
                modifier = Modifier.size(width = 332.dp, height = 80.dp)
            )
        }
    }
}
