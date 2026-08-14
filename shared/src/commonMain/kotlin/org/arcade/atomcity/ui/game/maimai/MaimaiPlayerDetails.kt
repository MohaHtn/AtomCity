package org.arcade.atomcity.ui.game.maimai

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.arcade.atomcity.model.maitea.playerDetailsResponse.PlayerDetailsData
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.ui.game.common.selectRatingBackground
import org.arcade.atomcity.utils.format
import kotlin.math.roundToInt

@Composable
fun MaimaiPlayerDetails(
    maiteaViewModel: MaiteaViewModel,
    collapsedFraction: Float,
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
fun ScrollingTitleText(
    modifier: Modifier = Modifier,
    text: String,
    collapsedFraction: Float,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    threshold: Int = 4
) {
    val textStyle = MaterialTheme.typography.titleMedium.copy(
        fontSize = lerp(22.sp, 17.sp, collapsedFraction),
        fontWeight = FontWeight.Black
    )

    // Si court, afficher simplement avec un fond
    if (text.length <= threshold) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = modifier
        ) {
            Text(
                text = text,
                style = textStyle,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        return
    }

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clipToBounds()
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        val density = LocalDensity.current
        val horizontalPaddingPx = with(density) { 12.dp.toPx() }
        val containerWidthPx = with(density) { maxWidth.toPx() } - horizontalPaddingPx
        var textWidthPx by remember(text, collapsedFraction) { mutableStateOf(0f) }

        val infiniteTransition = rememberInfiniteTransition()
        val shouldScroll = text.length > threshold || textWidthPx > containerWidthPx

        val overflowPx = remember(textWidthPx, containerWidthPx) {
            (textWidthPx - containerWidthPx).coerceAtLeast(0f)
        }

        val animDurationMs = remember(overflowPx, shouldScroll) {
            if (!shouldScroll) 0 else ((overflowPx * 25).toInt().coerceAtLeast(2000))
        }

        val offsetPx by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = if (shouldScroll) -overflowPx else 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = animDurationMs, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        Text(
            text = text,
            style = textStyle,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            onTextLayout = { layoutResult ->
                textWidthPx = layoutResult.size.width.toFloat()
            },
            modifier = Modifier.offset { IntOffset(offsetPx.roundToInt(), 0) }
        )
    }
}

@Composable
fun MaimaiPlayerDetailsContent(
    playerData: PlayerDetailsData?,
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
            val imageUrl = playerData?.options?.iconDeka?.png ?: playerData?.options?.icon?.png
            
            if (imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            Spacer(modifier = Modifier.width(if (isNarrow) 8.dp else 12.dp))

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = playerData?.name ?: "",
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

                MaimaiRatingBadge(
                    rating = playerData?.rating,
                    modifier = Modifier.padding(top = 4.dp),
                    fontSize = if (isNarrow) 11.sp else 13.sp
                )
            }
        }
    }
}
