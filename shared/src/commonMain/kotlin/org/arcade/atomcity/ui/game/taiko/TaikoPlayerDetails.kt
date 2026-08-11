package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel

@Composable
fun TaikoPlayerDetails(
    taikoViewModel: TaikoViewModel,
    collapsedFraction: Float,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val userSettings by taikoViewModel.userSettingsData.collectAsState()

    TaikoPlayerDetailsContent(
        name = userSettings?.myDonName,
        title = userSettings?.title,
        collapsedFraction = collapsedFraction,
        textColor = textColor
    )
}

@Composable
fun TaikoPlayerDetailsContent(
    name: String?,
    title: String?,
    collapsedFraction: Float,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isNarrow = maxWidth < 260.dp
        val avatarSize = if (isNarrow) {
            lerp(56.dp, 40.dp, collapsedFraction)
        } else {
            lerp(64.dp, 46.dp, collapsedFraction)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            // Placeholder for Avatar (Taiko avatar is complex/merged)
            Surface(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(MaterialTheme.shapes.small),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = (name?.take(1) ?: "T").uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(if (isNarrow) 8.dp else 12.dp))

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name ?: "Chargement...",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = if (isNarrow) {
                            lerp(18.sp, 15.sp, collapsedFraction)
                        } else {
                            lerp(20.sp, 17.sp, collapsedFraction)
                        },
                        fontWeight = FontWeight.Black
                    ),
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (title != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = if (isNarrow) 10.sp else 12.sp,
                                letterSpacing = 0.sp
                            ),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
