package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.arcade.atomcity.ui.core.AutoResizedText
import org.arcade.atomcity.ui.game.common.isAppInDarkTheme
import org.arcade.atomcity.ui.theme.NijiiroFontFamily

@Composable
fun TaikoNameplate(
    playerName: String?,
    title: String?,
    nameplateUrls: List<String>,
    collapsedFraction: Float = 0f,
    modifier: Modifier = Modifier,
    isNarrow: Boolean = false,
    textModifier: Modifier = Modifier.padding(horizontal = 12.dp),
    titleOffsetX: Dp = 0.dp,
    titleOffsetY: Dp = 0.dp,
    nameOffsetX: Dp = 0.dp,
    nameOffsetY: Dp = 0.dp,
    titleFontSize: TextUnit? = null,
    nameFontSize: TextUnit? = null
) {
    val isDark = isAppInDarkTheme()

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val virtualWidth = 830f
        val virtualHeight = 200f

        val scale = minOf(
            maxWidth.value / virtualWidth,
            maxHeight.value / virtualHeight
        )

        val multiplier = 2.5f

        val hasDan = nameplateUrls.any { it.contains("nameplate_dan") }
        val danPadding = if (hasDan) 280.dp else 0.dp

        val vTitleOffsetX = (titleOffsetX.value * multiplier).dp
        val vTitleOffsetY = (titleOffsetY.value * multiplier).dp
        val vNameOffsetX = (nameOffsetX.value * multiplier).dp
        val vNameOffsetY = (nameOffsetY.value * multiplier).dp

        val vTitleFontSize = ((titleFontSize?.value ?: 10f) * multiplier).sp
        val vNameFontSize = ((nameFontSize?.value ?: 14f) * multiplier).sp

        Box(
            modifier = Modifier
                .requiredSize(virtualWidth.dp, virtualHeight.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            val nameplateBackgroundAlpha = collapsedFraction.coerceIn(0f, 1f)
            val nameplateDarkOverlayAlpha = ((1f - collapsedFraction) * 0.15f).coerceIn(0f, 0.15f)

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(12.dp)
                    .background(
                        color = Color.Black.copy(alpha = nameplateDarkOverlayAlpha),
                        shape = RoundedCornerShape(45.dp)
                    )
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(nameplateBackgroundAlpha)
            ) {
                val danPlates = nameplateUrls.filter { it.contains("nameplate_dan") }
                val specialPlates = nameplateUrls.filter { it.contains("AprilFool") || it.contains("Toho") }
                val basePlates = nameplateUrls.filterNot { it.contains("nameplate_dan") || it.contains("AprilFool") || it.contains("Toho") }

                // 1. Draw baseplates
                basePlates.forEach { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }

                // 2. Draw special plates
                specialPlates.forEach { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .matchParentSize()
                            .wrapContentHeight(unbounded = true, align = Alignment.Bottom)
                            .zIndex(1f),
                        contentScale = ContentScale.FillWidth,
                        alignment = Alignment.BottomCenter
                    )
                }

                // 3. Draw Dan overlay
                danPlates.forEach { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .matchParentSize()
                            .wrapContentHeight(unbounded = true, align = Alignment.Bottom)
                            .zIndex(2f),
                        contentScale = ContentScale.FillWidth,
                        alignment = Alignment.BottomCenter
                    )
                }
            }

            val nameMinFontSize = if (isNarrow) 7.sp else 8.sp
            val player = playerName ?: ""

            Column(
                modifier = Modifier
                    .matchParentSize()
                    .then(textModifier)
            ) {
                if (collapsedFraction < 0.5f) {
                    // Expanded mode
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!title.isNullOrBlank()) {
                            AutoResizedText(
                                text = title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = NijiiroFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = vTitleFontSize,
                                    letterSpacing = 0.sp
                                ),
                                color = if (isDark) Color.White else Color.Black,
                                maxLines = 1,
                                minFontSize = 7.sp,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        OutlinedNameText(
                            text = player,
                            fontSize = vNameFontSize,
                            strokeWidth = 30f,
                            minFontSize = nameMinFontSize,
                            modifier = Modifier.offset(x = vNameOffsetX, y = vNameOffsetY)
                        )
                    }
                } else {
                    // Compact mode
                    val titleLineHeight = if (isNarrow) 10.sp else vTitleFontSize

                    Box(
                        modifier = Modifier
                            .weight(0.38f)
                            .fillMaxWidth()
                            .offset(x = vTitleOffsetX, y = vTitleOffsetY),
                        contentAlignment = Alignment.Center
                    ) {
                        AutoResizedText(
                            text = title ?: "",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = NijiiroFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = vTitleFontSize,
                                letterSpacing = 0.sp,
                                lineHeight = titleLineHeight
                            ),
                            color = Color.Black,
                            maxLines = 1,
                            minFontSize = 6.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(0.62f)
                            .fillMaxWidth()
                            .offset(x = vNameOffsetX, y = vNameOffsetY)
                            .padding(start = danPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedNameText(
                            text = player,
                            fontSize = vNameFontSize,
                            strokeWidth = 30f,
                            minFontSize = nameMinFontSize,
                            lineHeight = vNameFontSize
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OutlinedNameText(
    text: String,
    fontSize: TextUnit,
    strokeWidth: Float,
    minFontSize: TextUnit,
    modifier: Modifier = Modifier,
    lineHeight: TextUnit = TextUnit.Unspecified
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val baseStyle = MaterialTheme.typography.titleMedium.copy(
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            fontFamily = NijiiroFontFamily
        )
        val style = if (lineHeight != TextUnit.Unspecified) baseStyle.copy(lineHeight = lineHeight) else baseStyle

        // Stroke layer
        AutoResizedText(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = style.copy(
                drawStyle = Stroke(
                    miter = 10f,
                    width = strokeWidth,
                    join = StrokeJoin.Round
                )
            ),
            color = Color.Black,
            maxLines = 1,
            minFontSize = minFontSize
        )

        // Fill layer
        AutoResizedText(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = style,
            color = Color.White,
            maxLines = 1,
            minFontSize = minFontSize
        )
    }
}

@Preview
@Composable
fun TaikoNameplateExpandedPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFF222222))
                .padding(12.dp)
                .size(width = 332.dp, height = 80.dp),
            contentAlignment = Alignment.Center
        ) {
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

@Preview
@Composable
fun TaikoNameplateCompactPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFF222222))
                .padding(12.dp)
                .size(width = 332.dp, height = 80.dp),
            contentAlignment = Alignment.Center
        ) {
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

