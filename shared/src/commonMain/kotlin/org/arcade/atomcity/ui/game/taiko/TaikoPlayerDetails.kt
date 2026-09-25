package org.arcade.atomcity.ui.game.taiko

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.arcade.atomcity.data.remote.model.taikoserver.usersettings.TaikoServerUserSettingsResponse
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel

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
    userSettings: TaikoServerUserSettingsResponse? = null,
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
            lerp(96.dp, 72.dp, collapsedFraction)
        } else {
            lerp(150.dp, 110.dp, collapsedFraction)
        }
        val nameplateHeight = if (isNarrow) {
            lerp(68.dp, 56.dp, collapsedFraction)
        } else {
            lerp(90.dp, 72.dp, collapsedFraction)
        }
        val nameplateYOffset = lerp(0.dp, 2.dp, collapsedFraction)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 0.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                Text(
                    text = "Taiko",
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = if (isNarrow) lerp(18.sp, 16.sp, collapsedFraction) else lerp(25.sp, 21.sp, collapsedFraction),
                    lineHeight = if (isNarrow) lerp(14.sp, 12.sp, collapsedFraction) else lerp(18.sp, 16.sp, collapsedFraction)
                )
                Text(
                    text = "no Tatsujin",
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = if (isNarrow) lerp(18.sp, 16.sp, collapsedFraction) else lerp(25.sp, 21.sp, collapsedFraction),
                    lineHeight = if (isNarrow) lerp(14.sp, 12.sp, collapsedFraction) else lerp(18.sp, 16.sp, collapsedFraction)
                )
            }

                VerticalDivider(
                    modifier = Modifier
                        .height(lerp(48.dp, 36.dp, collapsedFraction))
                        .padding(horizontal = 4.dp),
                    thickness = 2.dp,
                    color = Color.Black
                )
            }


            // Taiko Avatar

            // Dynamic Y offset, ensuring for all screens that the avatar will be centered
            // It was a pain in the ass, fuck
            val dynamicYOffset = -(avatarSize * 0.10f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .offset(y = dynamicYOffset),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.requiredSize(avatarSize + 14.dp),
                    contentAlignment = Alignment.Center
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
                        val infiniteTransition = rememberInfiniteTransition()
                        val puchiOffsetY by infiniteTransition.animateFloat(
                            initialValue = -3f,
                            targetValue = 3f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = EaseInOutSine),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        AsyncImage(
                            model = taikoViewModel.getCostumeImageUrl("puchi", userSettings.puchi),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(y = puchiOffsetY.dp)
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
                            // Compensate for the avatar size
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(24.dp)
                                    .offset(y = lerp(16.dp, 0.dp, collapsedFraction))
                            )
                        }
                    }
                }
                }
            }

            val nameplateModifier = Modifier
                .fillMaxWidth()
                .height(nameplateHeight)
                .offset(y = if (isNarrow) 0.dp else nameplateYOffset)

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                TaikoNameplate(
                playerName = name,
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
}
