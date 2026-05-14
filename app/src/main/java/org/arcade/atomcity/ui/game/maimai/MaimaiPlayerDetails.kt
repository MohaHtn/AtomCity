package org.arcade.atomcity.ui.game.maimai

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.arcade.atomcity.model.maitea.playerDetailsResponse.Data
import org.arcade.atomcity.model.maitea.playerDetailsResponse.Icon
import org.arcade.atomcity.model.maitea.playerDetailsResponse.Options
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.ui.game.common.selectRatingBackgroundColor


@Composable
fun MaimaiPlayerDetails(
    maiteaViewModel: MaiteaViewModel,
    collapsedFraction: Float,
    onBackClick: () -> Unit,
    topAppBarWidth: Dp,
    topAppBarHeight: Dp,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val playerDataState by maiteaViewModel.playerData.collectAsState()
    val playerData = playerDataState?.data?.firstOrNull()

    LaunchedEffect(Unit) {
        maiteaViewModel.fetchMaimaiPlayerDetails()
    }

    MaimaiPlayerDetailsContent(
        playerData = playerData,
        collapsedFraction = collapsedFraction,
        textColor = textColor
    )
}

@Composable
fun MaimaiPlayerDetailsContent(
    playerData: Data?,
    collapsedFraction: Float,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    fun computeRating(rating: Int?): String {
        if (rating != null) {
            val s = rating.toString()
            return when {
                rating <= 1000 -> if (s.length >= 2) s.substring(0, s.length - 2) + "." + s.substring(s.length - 2) else s
                s.length >= 4 -> s.substring(0, s.length - 2) + "." + s.substring(s.length - 2)
                else -> s
            }
        }
        return "0.00"
    }

    fun getContrastingColor(backgroundColor: Color): Color {
        val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
        return if (luminance > 0.5f) Color.Black else Color.White
    }

    val ratingBgColor = selectRatingBackgroundColor(playerData?.rating)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        val imageUrl = playerData?.options?.iconDeka?.png ?: playerData?.options?.icon?.png
        
        if (imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(lerp(64.dp, 32.dp, collapsedFraction))
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = playerData?.name ?: "Chargement...",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = lerp(22.sp, 17.sp, collapsedFraction),
                    fontWeight = FontWeight.Black
                ),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Surface(
                color = ratingBgColor,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(top = 2.dp),
                tonalElevation = 4.dp
            ) {
                Text(
                    text = computeRating(playerData?.rating),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        letterSpacing = 0.sp
                    ),
                    color = getContrastingColor(ratingBgColor),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Expanded")
@Composable
fun PreviewMaimaiPlayerDetailsExpanded() {
    MaterialTheme {
        MaimaiPlayerDetailsContent(
            playerData = Data(
                name = "AIME_USER",
                rating = 1543,
                options = Options(
                    icon = Icon(png = "https://example.com/icon.png")
                )
            ),
            collapsedFraction = 0f
        )
    }
}

@Preview(showBackground = true, name = "Collapsed")
@Composable
fun PreviewMaimaiPlayerDetailsCollapsed() {
    MaterialTheme {
        Box(modifier = Modifier.background(Color.DarkGray).padding(8.dp)) {
            MaimaiPlayerDetailsContent(
                playerData = Data(
                    name = "AIME_USER",
                    rating = 1543,
                    options = Options(
                        icon = Icon(png = "https://example.com/icon.png")
                    )
                ),
                collapsedFraction = 1f
            )
        }
    }
}
