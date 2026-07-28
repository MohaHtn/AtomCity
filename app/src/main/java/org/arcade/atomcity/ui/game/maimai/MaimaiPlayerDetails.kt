package org.arcade.atomcity.ui.game.maimai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.animation.core.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.SolidColor
import kotlin.math.roundToInt
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.arcade.atomcity.model.maitea.playerDetailsResponse.Data
import org.arcade.atomcity.model.maitea.playerDetailsResponse.Icon
import org.arcade.atomcity.model.maitea.playerDetailsResponse.Options
import org.arcade.atomcity.model.maitea.playerDetailsResponse.Title
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.ui.game.common.selectRatingBackground


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

    val ratingBackground = selectRatingBackground(playerData?.rating)
    val ratingTextColor = if (ratingBackground is SolidColor) {
        getContrastingColor(ratingBackground.value)
    } else {
        Color.White
    }

    val shouldShowCompactInfo = collapsedFraction > 0.5f

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
                contentDescription = "Profile Avatar",
                contentScale = ContentScale.Crop,
                modifier =
                    if (collapsedFraction < 0.5f) {
                        Modifier
                            .absoluteOffset(y = (-8).dp)
                            .size(lerp(70.dp, 46.dp, collapsedFraction))
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    } else {
                        Modifier
                            .size(lerp(70.dp, 46.dp, collapsedFraction))
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    }

            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(
            verticalArrangement = Arrangement.Center,
            modifier = if (collapsedFraction < 0.5f) {
                Modifier.weight(1f).absoluteOffset(y = (-12).dp)
            } else {
                Modifier.weight(1f)
            }
        ) {
            Text(
                text = playerData?.name ?: "",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = lerp(22.sp, 17.sp, collapsedFraction),
                    fontWeight = FontWeight.Black
                ),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!shouldShowCompactInfo) {
                ScrollingTitleText(
                    text = playerData?.options?.title?.value ?: "",
                    collapsedFraction = collapsedFraction,
                    textColor = textColor,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .padding(top = 4.dp)
                    .background(ratingBackground, RoundedCornerShape(4.dp)),
                color = Color.Transparent,
                tonalElevation = 4.dp
            ) {
                Text(
                    text = computeRating(playerData?.rating),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        letterSpacing = 0.sp
                    ),
                    color = ratingTextColor,
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

@Preview(showBackground = true, name = "Collapsed")
@Composable
fun PreviewMaimaiPlayerDetailsCollapsed() {
    MaterialTheme {
        Box(modifier = Modifier.background(Color.DarkGray).padding(8.dp)) {
            MaimaiPlayerDetailsContent(
                playerData = Data(
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
