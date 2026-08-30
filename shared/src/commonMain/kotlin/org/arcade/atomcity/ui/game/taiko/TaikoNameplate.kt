package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import org.arcade.atomcity.ui.theme.NijiiroFontFamily

@Composable
fun TaikoNameplate(
    name: String?,
    title: String?,
    nameplateUrls: List<String>,
    collapsedFraction: Float = 0f,
    modifier: Modifier = Modifier,
    isNarrow: Boolean = false,
    textModifier: Modifier = Modifier.padding(horizontal = 12.dp)
) {
    Box(
        modifier = modifier
    ) {
        val nameplateBackgroundAlpha = collapsedFraction.coerceIn(0f, 1f)
        val nameplateDarkOverlayAlpha = ((1f - collapsedFraction) * 0.15f).coerceIn(0f, 0.15f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .background(
                    color = Color.Black.copy(alpha = nameplateDarkOverlayAlpha),
                    shape = RoundedCornerShape(24.dp)
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(nameplateBackgroundAlpha)
        ) {
            val danPlates = nameplateUrls.filter { it.contains("nameplate_dan") }
            val specialPlates = nameplateUrls.filter { it.contains("AprilFool") || it.contains("Toho") }
            val basePlates = nameplateUrls.filterNot { it.contains("nameplate_dan") || it.contains("AprilFool") || it.contains("Toho") }

            // 1. Draw base plates (FillBounds) - includes nameplate.webp and colored frames
            basePlates.forEach { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }

            // 2. Draw special plates (Maintain aspect ratio, allow overflow for characters)
            specialPlates.forEach { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y=12.dp)
                        .wrapContentHeight(unbounded = true, align = Alignment.CenterVertically)
                        .zIndex(1f),
                    contentScale = ContentScale.FillWidth,
                    alignment = Alignment.BottomCenter
                )
            }

            // 3. Draw Dan overlay (Always on top)
            danPlates.forEach { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().zIndex(2f),
                    contentScale = ContentScale.FillBounds
                )
            }
        }

        val isDanPlate = nameplateUrls.any { it.contains("nameplate_dan") }

        if (isDanPlate) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(textModifier)
            ) {
                val titleWeight = 0.55f * collapsedFraction
                // Title area (Teal top part)
                if (titleWeight > 0.05f) {
                    Box(
                        modifier = Modifier
                            .weight(titleWeight)
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                            .graphicsLayer { alpha = collapsedFraction },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title ?: "",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = NijiiroFontFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = if (isNarrow) 10.sp else 14.sp,
                                letterSpacing = 0.sp
                            ),
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.offset(y = -(2.5).dp)
                        )
                    }
                }

                // Name area (Bottom part with White box on right)
                Row(
                    modifier = Modifier
                        .weight(1f - titleWeight)
                        .fillMaxWidth()
                ) {
                    val spacerWeight = 0.42f * collapsedFraction
                    if (spacerWeight > 0.05f) {
                        Spacer(modifier = Modifier.weight(spacerWeight))
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f - spacerWeight)
                            .fillMaxSize(),
                        contentAlignment = if (collapsedFraction < 0.5f) Alignment.Center else Alignment.BottomCenter
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(bottom = if (collapsedFraction > 0.8f) 4.dp else 0.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (collapsedFraction < 0.8f) {
                                Text(
                                    text = title ?: "",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = NijiiroFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isNarrow) 10.sp else 14.sp,
                                        letterSpacing = 0.sp
                                    ),
                                    color = Color.Black.copy(alpha = (1f - collapsedFraction).coerceIn(0f, 1f)),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(bottom = 2.dp).offset(y = (1).dp)
                                )
                            }

                            Box(
                                modifier = Modifier.fillMaxWidth().offset(y = if (collapsedFraction > 0.8f) (-4).dp else 0.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val fontSize = if (isNarrow) {
                                    if (collapsedFraction > 0.8f) 11.sp else 13.sp
                                } else {
                                    if (collapsedFraction > 0.8f) 14.sp else 17.sp
                                }
                                val nameText = name ?: ""

                                // "Border/Stroke" layer
                                Text(
                                    text = nameText,
                                    modifier = Modifier.fillMaxWidth().offset(y=-(2).dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = fontSize,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = NijiiroFontFamily,
                                        drawStyle = Stroke(
                                            miter = 10f,
                                            width = 12f,
                                            join = StrokeJoin.Round
                                        )
                                    ),
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    softWrap = false
                                )
                                // "Main" layer
                                Text(
                                    text = nameText,
                                    modifier = Modifier.fillMaxWidth().offset(y=-(2).dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = fontSize,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = NijiiroFontFamily
                                    ),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(textModifier)
            ) {
                val titleWeight = 0.5f * collapsedFraction
                // Title area (Top colored section)
                if (titleWeight > 0.05f) {
                    Box(
                        modifier = Modifier
                            .weight(titleWeight)
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                            .graphicsLayer { alpha = collapsedFraction },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title ?: "",
                            modifier = Modifier.offset(y = -(1).dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = NijiiroFontFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = if (isNarrow) 10.sp else 14.sp,
                                letterSpacing = 0.sp
                            ),
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // Name area (Bottom white section)
                Box(
                    modifier = Modifier
                        .weight(1f - titleWeight)
                        .fillMaxSize()
                        .padding(bottom = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (collapsedFraction < 0.8f) {
                            Text(
                                text = title ?: "",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = NijiiroFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isNarrow) 10.sp else 12.sp,
                                    letterSpacing = 0.sp
                                ),
                                color = Color.Black.copy(alpha = (1f - collapsedFraction).coerceIn(0f, 1f)),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        val fontSize = if (isNarrow) {
                            if (collapsedFraction > 0.8f) 11.sp else 13.sp
                        } else {
                            if (collapsedFraction > 0.8f) 14.sp else 17.sp
                        }
                        val nameText = name ?: ""

                        Box(
                            modifier = Modifier.fillMaxWidth().offset(y = if (collapsedFraction > 0.8f) (-4).dp else 0.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // "Border/Stroke" layer
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = nameText,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = fontSize,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = NijiiroFontFamily,
                                    drawStyle = Stroke(
                                        miter = 10f,
                                        width = 12f,
                                        join = StrokeJoin.Round
                                    )
                                ),
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )
                            // "Main" layer
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = nameText,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = fontSize,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = NijiiroFontFamily
                                ),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}
