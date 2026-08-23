package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
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
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            // Taiko Avatar Rendering
            Box(
                modifier = Modifier
                    .size(avatarSize + 14.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (taikoViewModel != null && userSettings != null) {
                    val avatarImageModifier = Modifier.fillMaxHeight()

                    // 1. Base Body layer (tinted)
                    AsyncImage(
                        model = taikoViewModel.getMaskImageUrl("body", "body", 0),
                        contentDescription = null,
                        modifier = avatarImageModifier,
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(taikoViewModel.getDonColor(userSettings.bodyColor))
                    )

                    // 2. Base Face layer (tinted)
                    AsyncImage(
                        model = taikoViewModel.getMaskImageUrl("body", "face", 0),
                        contentDescription = null,
                        modifier = avatarImageModifier,
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(taikoViewModel.getDonColor(userSettings.faceColor))
                    )

                    // 3. Puchi layer
                    if (userSettings.puchi != null && userSettings.puchi != 0) {
                        AsyncImage(
                            model = taikoViewModel.getCostumeImageUrl("puchi", userSettings.puchi),
                            contentDescription = null,
                            modifier = avatarImageModifier,
                            contentScale = ContentScale.Fit
                        )
                    }

                    // 4. Face Accessory layer
                    if (userSettings.face != null && userSettings.face != 0) {
                        AsyncImage(
                            model = taikoViewModel.getCostumeImageUrl("face", userSettings.face),
                            contentDescription = null,
                            modifier = avatarImageModifier,
                            contentScale = ContentScale.Fit
                        )
                    }

                    // 5. Costume layers (Kigurumi overrides Body and Head)
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

            Spacer(modifier = Modifier.width(if (isNarrow) 8.dp else 12.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(avatarSize)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Nameplate Background layers
                Box(modifier = Modifier.fillMaxSize()) {
                    nameplateUrls.forEach { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
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
                                    .offset(x = if (isNarrow) (-4).dp else (-12).dp)
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
                                        overflow = TextOverflow.Visible
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
                                        overflow = TextOverflow.Ellipsis
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
                                overflow = TextOverflow.Ellipsis
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
                                    text = name ?: "",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = fontSize,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = NijiiroFontFamily
                                    ),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
