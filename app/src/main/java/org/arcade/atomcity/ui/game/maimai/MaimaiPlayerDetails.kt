package org.arcade.atomcity.ui.game.maimai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiPlayerDetails(
    maiteaViewModel: MaiteaViewModel,
    collaspedFraction : Float,
    onBackClick: () -> Unit,
    topAppBarWidth: Dp,
    topAppBarHeight: Dp,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val playerData = maiteaViewModel.playerData.collectAsState().value?.data[0]

    fun computeRating(rating: Int?): String {
        if (rating != null) {
            return when {
                rating <= 1000 -> rating.toString().substring(0) + "." + rating.toString().substring(2)
                else -> rating.toString().substring(0, 2) + "." + rating.toString().substring(2, 4)
            }
        }
        return "0.00"
    }

    fun selectRatingBackgroundColor(rating: Int?): Color {
        return when (rating) {
            null -> Color.Transparent
            in 0..200 -> Color.White
            in 200..399 -> Color.Blue
            in 400..699 -> Color.Green
            in 700..999 -> Color.Cyan
            in 1000..1199 -> Color.Red
            in 1200..1299 -> Color.Magenta
            in 1300..1399 -> Color(0xFFA52A2A) // Brown
            in 1400..1449 -> Color.Gray
            in 1450..1499 -> Color.Yellow
            else -> Color.Black
        }
    }
     fun getContrastingColor(backgroundColor: Color): Color {
        // On calcule la luminosité de la couleur (formule standard)
        val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
        // Texte noir si le fond est clair, texte blanc si le fond est foncé
        return if (luminance > 0.5f) Color.Black else Color.White
    }

    LaunchedEffect(Unit) {
        maiteaViewModel.fetchMaimaiPlayerDetails()
    }

    Box(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)

    )
    {
//        playerData?.options?.frame?.png.let { imageUrl ->
//            AsyncImage(
//                model = ImageRequest.Builder(LocalContext.current)
//                    .data(imageUrl)
//                    .crossfade(true)
//                    .build(),
//                contentDescription = "Background Frame of ${playerData?.name}",
//                modifier = Modifier.fillMaxSize(),
//                contentScale = ContentScale.Crop,
//                alpha = 0.5f
//            )

        Row{
            playerData?.options?.iconDeka?.png.let { imageUrl ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar of ${playerData?.name}",
                    modifier = Modifier
                        .size(100.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
        Column(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).padding(start = 100.dp)
        ) {
            Text(text = playerData?.name ?: "Chargement ...",
                fontSize = if (collaspedFraction < 0.5f) 20.sp else 16.sp,
                fontWeight = if (collaspedFraction > 0.5f)
                                    androidx.compose.ui.text.font.FontWeight.Bold
                                else
                                    androidx.compose.ui.text.font.FontWeight.Normal,
                 color = if (collaspedFraction > 0.5f)
                                    Color.White
                                else
                                    Color.Black,
            )
            Text(
                text = computeRating(playerData?.rating),
                fontSize = if (collaspedFraction < 0.5f) 20.sp else 16.sp,
                color = getContrastingColor(selectRatingBackgroundColor(playerData?.rating))
                ,
                modifier = Modifier
                    .padding(
                        if (collaspedFraction < 0.5f) {
                            8.dp
                        } else {
                            0.dp
                        }
                    ) // Ajoute une marge en haut
                    .background(
                    color = selectRatingBackgroundColor(playerData?.rating),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
    }
