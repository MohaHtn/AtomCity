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
    val density = LocalDensity.current
    var componentWidth by remember { mutableStateOf(0.dp) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .onSizeChanged { size ->
                componentWidth = with(density) { size.width.toDp() }
            }
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = (1f - collapsedFraction).coerceIn(0f, 0.7f)),
                shape = RoundedCornerShape(16.dp),
            )
    ) {
        val isNarrow = componentWidth < 260.dp && componentWidth > 0.dp
        val avatarSize = if (isNarrow) {
            lerp(80.dp, 56.dp, collapsedFraction)
        } else {
            lerp(110.dp, 64.dp, collapsedFraction)
        }

        Row {
            Column( modifier = Modifier.align(Alignment.CenterVertically).padding(8.dp)){
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

            TaikoNameplate(
                name = name,
                title = title,
                nameplateUrls = nameplateUrls,
                collapsedFraction = collapsedFraction,
                modifier = Modifier
                    .weight(1f)
                    .height(avatarSize),
                isNarrow = isNarrow
            )
        }
    }
}
