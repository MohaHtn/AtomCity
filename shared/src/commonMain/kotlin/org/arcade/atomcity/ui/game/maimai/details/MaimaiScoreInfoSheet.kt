package org.arcade.atomcity.ui.game.maimai.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherApiData
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.formatScoreValue
import org.arcade.atomcity.ui.core.AutoResizedText
import org.arcade.atomcity.utils.format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiScoreInfoSheet(
    scoreEntry: ScorefetcherApiData,
    difficultyColor: Color,
    onDismissRequest: () -> Unit
) {
    val detail = scoreEntry.scoreDetail
    val tapCount = (detail?.tap?.perfect ?: 0) + (detail?.tap?.great ?: 0) +
            (detail?.tap?.good ?: 0) + (detail?.tap?.bad ?: 0)
    val holdCount = (detail?.hold?.perfect ?: 0) + (detail?.hold?.great ?: 0) +
            (detail?.hold?.good ?: 0) + (detail?.hold?.bad ?: 0)
    val slideCount = (detail?.slide?.perfect ?: 0) + (detail?.slide?.great ?: 0) +
            (detail?.slide?.good ?: 0) + (detail?.slide?.bad ?: 0)
    val breakCount = (detail?.breakk?.perfect ?: 0) + (detail?.breakk?.great ?: 0) +
            (detail?.breakk?.good ?: 0) + (detail?.breakk?.bad ?: 0)

    val tapPts = tapCount * 500
    val holdPts = holdCount * 1000
    val slidePts = slideCount * 1500
    val breakPts = (breakCount * 2500 * 1.04).toLong()
    val totalPts = tapPts + holdPts + slidePts + breakPts

    val basePts = (tapCount * 500) + (holdCount * 1000) + (slideCount * 1500) + (breakCount * 2500)
    val breakBonusPts = breakCount * 2500 * 0.04

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = difficultyColor
                )
                Text(
                    text = "Calcul du score et du pourcentage maximal possible",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Text(
                text = "Le calcul du score se fait de la manière suivante :",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AutoResizedText(
                    text = "($tapCount × 500) + ($holdCount × 1000) + ($slideCount × 1500) + ($breakCount × 2500 × 1,04)",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    minFontSize = 8.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "DÉTAIL PAR TYPE DE NOTE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val items = listOf(
                "Taps ($tapCount)" to "$tapCount × 500 = ${formatScoreValue(tapPts.toDouble())}",
                "Holds ($holdCount)" to "$holdCount × 1000 = ${formatScoreValue(holdPts.toDouble())}",
                "Slides ($slideCount)" to "$slideCount × 1500 = ${formatScoreValue(slidePts.toDouble())}",
                "Breaks ($breakCount)" to "$breakCount × 2500 × 1,04 = ${formatScoreValue(breakPts.toDouble())}"
            )

            items.forEach { (label, calc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = calc,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "maimai donne 100 points de bonus, ce qui équivaut à 4% (d'où 1,04) du score du BREAK tapée parfaitement (2500 points), ce qui donne au total 2600.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Score MAX Théorique",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = formatScoreValue(totalPts.toDouble()),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = difficultyColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "CALCUL DU POURCENTAGE MAXIMAL",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Le pourcentage maximal est le score avec les 4% de bonus BREAK sur le score moins ces 4% de bonus BREAK, " +
                                "ramené à 100, soit une différence de +${formatScoreValue(breakBonusPts)} points pour cette chart " +
                                "($breakCount × 100 pts). \n\nOn enlève ensuite 0,0045%, afin d'éviter des décimales infinies pendant le calcul en pourcentage " +
                                "(marge de troncature).",
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val maxPercent = scoreEntry.theoreticalMaxPercent

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val scaleFactor = when {
                        maxWidth < 300.dp -> 0.65f
                        maxWidth < 350.dp -> 0.75f
                        maxWidth < 400.dp -> 0.85f
                        else -> 1.0f
                    }

                    val numDenomFontSize = (13 * scaleFactor).sp
                    val symbolFontSize = (18 * scaleFactor).sp

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "( ",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = symbolFontSize,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 1,
                                softWrap = false
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(IntrinsicSize.Max)
                            ) {
                                AutoResizedText(
                                    text = "Score avec bonus BREAK de 4% (${formatScoreValue(totalPts.toDouble())})",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = numDenomFontSize,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    textAlign = TextAlign.Center,
                                    minFontSize = 7.sp,
                                    maxLines = 1
                                )

                                HorizontalDivider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = (3 * scaleFactor).dp),
                                    thickness = (1.5 * scaleFactor).dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )

                                AutoResizedText(
                                    text = "Score sans bonus BREAK de 4% (${formatScoreValue(basePts.toDouble())})",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = numDenomFontSize,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    textAlign = TextAlign.Center,
                                    minFontSize = 7.sp,
                                    maxLines = 1
                                )
                            }

                            Text(
                                text = " × 100 )",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = symbolFontSize,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Text(
                            text = "- 0,0045%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = (15 * scaleFactor).sp
                            ),
                            modifier = Modifier.padding(top = (8 * scaleFactor).dp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pourcentage MAX Théorique",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = "${maxPercent?.format(2) ?: "0.00"}%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = difficultyColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
