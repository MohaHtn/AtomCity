package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
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
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    titleOffsetX: Dp = 0.dp,
    titleOffsetY: Dp = 0.dp,
    nameOffsetX: Dp = 0.dp,
    nameOffsetY: Dp = 0.dp,
    titleFontSize: TextUnit? = null,
    nameFontSize: TextUnit? = null
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
        userSettings = userSettings,
        titleOffsetX = titleOffsetX,
        titleOffsetY = titleOffsetY,
        nameOffsetX = nameOffsetX,
        nameOffsetY = nameOffsetY,
        titleFontSize = titleFontSize,
        nameFontSize = nameFontSize
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
    userSettings: org.arcade.atomcity.data.remote.model.taikoserver.usersettings.TaikoServerUserSettingsResponse? = null,
    titleOffsetX: Dp = 0.dp,
    titleOffsetY: Dp = 0.dp,
    nameOffsetX: Dp = 0.dp,
    nameOffsetY: Dp = 0.dp,
    titleFontSize: TextUnit? = null,
    nameFontSize: TextUnit? = null
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = -(6.5).dp)
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = (1f - collapsedFraction).coerceIn(0f, 0.7f)),
                shape = RoundedCornerShape(16.dp),
            )
    ) {
        val isNarrow = maxWidth <= 360.dp
        val avatarSize = if (isNarrow) {
            lerp(72.dp, 48.dp, collapsedFraction)
        } else {
            lerp(110.dp, 64.dp, collapsedFraction)
        }
        val nameplateHeight = if (isNarrow) {
            lerp(68.dp, 48.dp, collapsedFraction)
        } else {
            lerp(90.dp, 52.dp, collapsedFraction)
        }
        val nameplateYOffset = lerp(0.dp, 2.dp, collapsedFraction)

        Row(
            modifier = Modifier.wrapContentWidth()
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 8.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
            ) {
                Text(
                    text = "Taiko",
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = if (isNarrow) lerp(18.sp, 14.sp, collapsedFraction) else lerp(25.sp, 16.sp, collapsedFraction),
                    lineHeight = if (isNarrow) lerp(14.sp, 11.sp, collapsedFraction) else lerp(16.sp, 12.sp, collapsedFraction)
                )
                Text(
                    text = "no Tatsujin",
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = if (isNarrow) lerp(18.sp, 14.sp, collapsedFraction) else lerp(25.sp, 16.sp, collapsedFraction),
                    lineHeight = if (isNarrow) lerp(14.sp, 11.sp, collapsedFraction) else lerp(16.sp, 12.sp, collapsedFraction)
                )
            }

            Column (modifier = Modifier.align(Alignment.CenterVertically)
            ){
                VerticalDivider(
                    modifier = Modifier
                        .height(lerp(36.dp, 24.dp, collapsedFraction))
                        .padding(horizontal = 4.dp),
                    thickness = 2.dp,
                    color = Color.Black
                )
            }


            // Taiko Avatar Rendering
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(0.dp)
                    .offset(y = if (!isNarrow) (-4).dp else 0.dp)
                    .requiredSize(avatarSize + 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (taikoViewModel != null && userSettings != null) {
                    val avatarImageModifier = Modifier.fillMaxSize()
                    val isKigurumi = (userSettings.kigurumi ?: 0) > 0
                    val faceColor = taikoViewModel.getDonColor(userSettings.faceColor)
                    val bodyColor = taikoViewModel.getDonColor(userSettings.bodyColor)
                    val limbColor = taikoViewModel.getDonColor(userSettings.limbColor)

                    val bodyId = userSettings.body ?: 0
                    val faceId = userSettings.face ?: 0
                    val headId = userSettings.head ?: 0

                    if (!isKigurumi) {
                        // 1. Color Masks (Bottom)
                        if (bodyId == 0) {
                            AsyncImage(
                                model = taikoViewModel.getMaskImageUrl("body", "body", 0),
                                contentDescription = null,
                                modifier = avatarImageModifier,
                                contentScale = ContentScale.Fit,
                                colorFilter = ColorFilter.tint(bodyColor)
                            )
                        }
                        if (faceId == 0) {
                            AsyncImage(
                                model = taikoViewModel.getMaskImageUrl("body", "face", 0),
                                contentDescription = null,
                                modifier = avatarImageModifier,
                                contentScale = ContentScale.Fit,
                                colorFilter = ColorFilter.tint(faceColor)
                            )
                        }
                        if (headId == 0) {
                            AsyncImage(
                                model = taikoViewModel.getMaskImageUrl("head", "head", 0),
                                contentDescription = null,
                                modifier = avatarImageModifier,
                                contentScale = ContentScale.Fit,
                                colorFilter = ColorFilter.tint(bodyColor)
                            )
                        }

                        // 2. Base Assets (Top) - Contain Outlines and White parts
                        // We draw body first, then face features, then head
                        AsyncImage(
                            model = taikoViewModel.getCostumeImageUrl("body", bodyId),
                            contentDescription = null,
                            modifier = avatarImageModifier,
                            contentScale = ContentScale.Fit
                        )
                        AsyncImage(
                            model = taikoViewModel.getCostumeImageUrl("face", faceId),
                            contentDescription = null,
                            modifier = avatarImageModifier,
                            contentScale = ContentScale.Fit
                        )
                        AsyncImage(
                            model = taikoViewModel.getCostumeImageUrl("head", headId),
                            contentDescription = null,
                            modifier = avatarImageModifier,
                            contentScale = ContentScale.Fit
                        )
                    }

                    if (isKigurumi) {
                        userSettings.kigurumi?.let { id ->
                            AsyncImage(
                                model = taikoViewModel.getCostumeImageUrl("kigurumi", id),
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
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 4.dp, end = 4.dp)
                                .align(Alignment.Center),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            val nameplateModifier = if (collapsedFraction >= 0.5f) {
                Modifier
                    .align(Alignment.CenterVertically)
                    .height(nameplateHeight)
                    .offset(y = if (isNarrow) 0.dp else nameplateYOffset)
                    .aspectRatio(4.15f, matchHeightConstraintsFirst = true)
            } else {
                Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
                    .height(nameplateHeight)
                    .offset(y = if (isNarrow) 0.dp else nameplateYOffset)
            }

            TaikoNameplate(
                name = name,
                title = title,
                nameplateUrls = nameplateUrls,
                collapsedFraction = collapsedFraction,
                modifier = nameplateModifier,
                isNarrow = isNarrow,
                titleOffsetX = titleOffsetX,
                titleOffsetY = titleOffsetY,
                nameOffsetX = nameOffsetX,
                nameOffsetY = nameOffsetY,
                titleFontSize = titleFontSize,
                nameFontSize = nameFontSize
            )
        }
    }
}
