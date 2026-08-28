package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.theme.NijiiroFontFamily

@Composable
fun TaikoPlayerDetails(
    taikoViewModel: TaikoViewModel,
    collapsedFraction: Float,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val userSettings by taikoViewModel.userDetailedSettings.collectAsState()
    val nameplateUrls = taikoViewModel.getNameplateUrls(userSettings)

    TaikoPlayerDetailsContent(
        name = userSettings?.myDonName,
        title = userSettings?.title,
        nameplateUrls = nameplateUrls,
        collapsedFraction = collapsedFraction,
        textColor = textColor,
        taikoViewModel = taikoViewModel,
        userSettings = userSettings
    )
}

@Composable
fun TaikoPlayerDetailsContent(
    name: String?,
    title: String?,
    nameplateUrls: List<String>,
    collapsedFraction: Float,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    taikoViewModel: TaikoViewModel? = null,
    userSettings: org.arcade.atomcity.data.remote.model.taikoserver.usersettings.TaikoServerUserSettingsResponse? = null
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isNarrow = maxWidth < 260.dp
        val avatarSize = if (isNarrow) {
            lerp(80.dp, 56.dp, collapsedFraction)
        } else {
            lerp(110.dp, 64.dp, collapsedFraction)
        }

        Row(

        ) {
            Column( modifier = Modifier.align(Alignment.CenterVertically)){
                Text(
                    text = "Taiko ",
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
                Text(
                    text = "no Tatstujin",
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
            }

            Column (modifier = Modifier.align(Alignment.CenterVertically)
            ){
                VerticalDivider(
                    modifier = Modifier
                        .height(36.dp)
                        .padding(horizontal = 8.dp),
                    thickness = 2.dp,
                    color = Color.Black
                )
            }


            // Taiko Avatar Rendering
            Box(
                modifier = Modifier
                    .padding(0.dp)
                    .requiredSize(avatarSize + 14.dp).offset(y = (-8).dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (taikoViewModel != null && userSettings != null) {
                    val avatarImageModifier = Modifier.fillMaxHeight()

                    AsyncImage(
                        model = taikoViewModel.getMaskImageUrl("body", "body", 0),
                        contentDescription = null,
                        modifier = avatarImageModifier,
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(taikoViewModel.getDonColor(userSettings.bodyColor))
                    )

                    AsyncImage(
                        model = taikoViewModel.getMaskImageUrl("body", "face", 0),
                        contentDescription = null,
                        modifier = avatarImageModifier,
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(taikoViewModel.getDonColor(userSettings.faceColor))
                    )


                    if (userSettings.face != null && userSettings.face != 0) {
                        AsyncImage(
                            model = taikoViewModel.getCostumeImageUrl("face", userSettings.face),
                            contentDescription = null,
                            modifier = avatarImageModifier,
                            contentScale = ContentScale.Fit
                        )
                    }

                    if (userSettings.kigurumi != null && userSettings.kigurumi != 0) {
                        AsyncImage(
                            model = taikoViewModel.getCostumeImageUrl("kigurumi", userSettings.kigurumi),
                            contentDescription = null,
                            modifier = avatarImageModifier,
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        if (userSettings.body != null && userSettings.body != 0) {
                            AsyncImage(
                                model = taikoViewModel.getCostumeImageUrl("body", userSettings.body),
                                contentDescription = null,
                                modifier = avatarImageModifier,
                                contentScale = ContentScale.Fit
                            )
                        }
                        if (userSettings.head != null && userSettings.head != 0) {
                            AsyncImage(
                                model = taikoViewModel.getCostumeImageUrl("head", userSettings.head),
                                contentDescription = null,
                                modifier = avatarImageModifier,
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    if (userSettings.puchi != null && userSettings.puchi != 0) {
                        AsyncImage(
                            model = taikoViewModel.getCostumeImageUrl("puchi", userSettings.puchi),
                            contentDescription = null,
                            modifier = avatarImageModifier,
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    // Fallback
                    Surface(
                        modifier = Modifier.fillMaxSize(),
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
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(avatarSize)

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
                    val baseNameplates = nameplateUrls.filterNot { it.contains("AprilFool") }
                    val aprilFoolNameplates = nameplateUrls.filter { it.contains("AprilFool") }

                    baseNameplates.forEach { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                    }

                    aprilFoolNameplates.forEach { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    translationY = -18f
                                    clip = false
                                }
                                .zIndex(1f),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }

                val isDanPlate = nameplateUrls.any { it.contains("nameplate_dan") }

                if (isDanPlate) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Title area (Teal top part)
                        Box(
                            modifier = Modifier
                                .weight(0.55f)
                                .fillMaxWidth()
                                .padding(start = if (isNarrow) 16.dp else 24.dp, end = 8.dp, top = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title ?: "",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = NijiiroFontFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = if (isNarrow) 10.sp else 14.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Name area (Bottom part with White box on right)
                        Row(
                            modifier = Modifier
                                .weight(0.45f)
                                .fillMaxWidth()
                        ) {
                            Spacer(modifier = Modifier.weight(0.42f)) // Covers the black area on the left
                            Box(
                                modifier = Modifier
                                    .weight(0.58f)
                                    .fillMaxSize(),
                                contentAlignment = Alignment.TopCenter
                            ) {

                                Box(contentAlignment = Alignment.Center) {
                                    val fontSize = if (isNarrow) 14.sp else 19.sp

                                    // "Border/Stroke" layer
                                    Text(
                                        text = name ?: "Chargement...",
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
                                        overflow = TextOverflow.Visible,
                                    )
                                    // "Main" layer
                                    Text(
                                        text = name ?: "Chargement...",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = fontSize,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = NijiiroFontFamily
                                        ),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,

                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Title area (Top colored section)
                        Box(
                            modifier = Modifier
                                .weight(0.5f)
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title ?: "",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = NijiiroFontFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = if (isNarrow) 10.sp else 12.sp,
                                ),
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        // Name area (Bottom white section)
                        Box(
                            modifier = Modifier
                                .weight(0.5f)
                                .fillMaxSize()
                                .padding(bottom = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val fontSize = if (isNarrow) 17.sp else 21.sp

                            Box(contentAlignment = Alignment.Center) {
                                // "Border/Stroke" layer
                                Text(
                                    modifier = Modifier.offset(y = (-4).dp),
                                    text = name ?: "",
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
                                    overflow = TextOverflow.Visible
                                )
                                // "Main" layer
                                Text(
                                    modifier = Modifier.offset(y = (-4).dp),
                                    text = name ?: "",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = fontSize,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = NijiiroFontFamily
                                    ),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
