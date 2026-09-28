package org.arcade.atomcity.ui.game.taiko.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.game.common.isAppInDarkTheme
import org.arcade.atomcity.ui.game.taiko.getDifficultyDrawable
import org.arcade.atomcity.ui.game.taiko.setDifficultyColorBackground
import org.arcade.atomcity.ui.theme.NijiiroFontFamily
import org.arcade.atomcity.utils.PlatformUtils
import org.arcade.atomcity.utils.format
import org.arcade.atomcity.utils.rememberPlatformContext
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Duration.Companion.milliseconds

private val DownloadIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Download",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(19f, 9f)
            lineTo(15f, 9f)
            lineTo(15f, 3f)
            lineTo(9f, 3f)
            lineTo(9f, 9f)
            lineTo(5f, 9f)
            lineTo(12f, 16f)
            lineTo(19f, 9f)
            close()
            moveTo(5f, 18f)
            horizontalLineToRelative(14f)
            verticalLineToRelative(2f)
            lineTo(5f, 20f)
            close()
        }
    }.build()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaikoProgress(
    onBackClick: () -> Unit,
    navController: NavHostController,
    taikoViewModel: TaikoViewModel,
) {
    val progressStats by taikoViewModel.progressStats.collectAsState()
    val overallProgressStats by taikoViewModel.overallProgressStats.collectAsState()
    val taikoUsers by taikoViewModel.taikoUsers.collectAsState()
    val isLoading by taikoViewModel.isLoadingStats.collectAsState()

    var isPersonnel by remember { mutableStateOf(true) }
    var selectedCommunityBaid by remember { mutableStateOf<Int?>(null) }
    var showUserMenu by remember { mutableStateOf(false) }

    var selectedShareStat by remember { mutableStateOf<TaikoProgressStats?>(null) }
    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()
    var isGeneratingImage by remember { mutableStateOf(false) }
    val context = rememberPlatformContext()

    val loggedInBaid = taikoViewModel.loggedInBaid

    // Exclure l'utilisateur connecté de la liste des autres joueurs
    val otherUsers = remember(taikoUsers, loggedInBaid) {
        if (loggedInBaid != null) {
            taikoUsers.filter { it.baid != loggedInBaid }
        } else {
            taikoUsers
        }
    }

    // Sélectionner automatiquement le premier joueur de la communauté si aucun n'est sélectionné
    LaunchedEffect(isPersonnel, otherUsers) {
        if (!isPersonnel && selectedCommunityBaid == null && otherUsers.isNotEmpty()) {
            selectedCommunityBaid = otherUsers.first().baid
        }
    }

    val activeBaid = if (isPersonnel) null else selectedCommunityBaid

    LaunchedEffect(Unit) {
        taikoViewModel.fetchProgress()
        taikoViewModel.fetchCommunityScores()
    }

    LaunchedEffect(activeBaid) {
        taikoViewModel.fetchProgressForBaid(activeBaid)
    }

    val selectedUser = remember(isPersonnel, selectedCommunityBaid, otherUsers) {
        if (!isPersonnel && selectedCommunityBaid != null) {
            otherUsers.find { it.baid == selectedCommunityBaid }
        } else null
    }

    val subtitleText = if (isPersonnel) {
        "Votre progression au sein du jeu"
    } else if (selectedUser != null) {
        "Statistiques de ${selectedUser.nickname ?: "Joueur #${selectedUser.baid}"}"
    } else {
        "Statistiques des autres joueurs"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Complétion",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            subtitleText,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        val haptic = LocalHapticFeedback.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Boutons de bascule: Mes stats vs Autres joueurs
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = isPersonnel,
                    onClick = {
                        if (!isPersonnel) {
                            isPersonnel = true
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            PlatformUtils.hapticImpact()
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Mes stats", fontWeight = FontWeight.SemiBold)
                }
                SegmentedButton(
                    selected = !isPersonnel,
                    onClick = {
                        if (isPersonnel) {
                            isPersonnel = false
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            PlatformUtils.hapticImpact()
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Autres joueurs", fontWeight = FontWeight.SemiBold)
                }
            }

            // Sélecteur de joueur animé quand "Autres joueurs" est actif
            AnimatedVisibility(
                visible = !isPersonnel,
                enter = fadeIn(tween(180)) + expandVertically(tween(180)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    ElevatedCard(
                        onClick = { showUserMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = selectedUser?.nickname ?: "Sélectionner un joueur",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Changer de joueur"
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showUserMenu,
                        onDismissRequest = { showUserMenu = false },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .heightIn(max = 280.dp)
                    ) {
                        otherUsers.forEach { user ->
                            val isSelected = user.baid == selectedCommunityBaid
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = user.nickname ?: "Joueur #${user.baid}",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    selectedCommunityBaid = user.baid
                                    showUserMenu = false
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    PlatformUtils.hapticImpact()
                                },
                                leadingIcon = {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Grille de cartes avec valeurs animées en interne
            Box(modifier = Modifier.fillMaxSize()) {
                if (progressStats.isNotEmpty() || overallProgressStats != null) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                    ) {
                        overallProgressStats?.let { overall ->
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                TotalProgressCard(overall)
                            }
                        }

                        items(progressStats, key = { it.difficulty }) { stat ->
                            ProgressCard(
                                stat = stat,
                                onShareClick = if (isPersonnel) {
                                    { selectedShareStat = it }
                                } else null
                            )
                        }
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (progressStats.isEmpty() && overallProgressStats == null) {
                                    MaterialTheme.colorScheme.surface
                                } else {
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    // Modal de partage / enregistrement de la carte de rang
    if (selectedShareStat != null) {
        val stat = selectedShareStat!!
        val (difficultyName, _) = when (stat.difficulty) {
            1 -> "Kantan" to "Facile"
            2 -> "Futsuu" to "Normal"
            3 -> "Muzukashii" to "Difficile"
            4 -> "Oni" to "Démoniaque"
            5 -> "Ura Oni" to "Ultra Démoniaque"
            else -> "Inconnu" to ""
        }

        ModalBottomSheet(
            onDismissRequest = { selectedShareStat = null },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Carte de rang - $difficultyName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                RankPanelLegendCard()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawWithContent {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }
                            drawContent()
                        }
                ) {
                    TaikoRankPanelCard(stat = stat)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bouton Enregistrer (Save) - Fonctionne sur iOS et Android
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    isGeneratingImage = true
                                    delay(350.milliseconds)
                                    val bitmap = graphicsLayer.toImageBitmap()
                                    PlatformUtils.saveImage(bitmap, context)
                                } catch (e: Exception) {
                                    PlatformUtils.log("TaikoProgress", "Error saving rank card: ${e.message}", true)
                                } finally {
                                    isGeneratingImage = false
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isGeneratingImage
                    ) {
                        Icon(DownloadIcon, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Enregistrer",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Bouton Partager (Share) - Fonctionne sur iOS et Android
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    isGeneratingImage = true
                                    delay(350.milliseconds)
                                    val bitmap = graphicsLayer.toImageBitmap()
                                    PlatformUtils.shareImage(bitmap, context)
                                } catch (e: Exception) {
                                    PlatformUtils.log("TaikoProgress", "Error sharing rank card: ${e.message}", true)
                                } finally {
                                    isGeneratingImage = false
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isGeneratingImage
                    ) {
                        if (isGeneratingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Partager",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaikoRankPanelCard(
    stat: TaikoProgressStats,
    modifier: Modifier = Modifier
) {
    val panelUrl = when (stat.difficulty) {
        1 -> "https://taiko.farewell.dev/images/rank_panel_Easy.webp"
        2 -> "https://taiko.farewell.dev/images/rank_panel_Normal.webp"
        3 -> "https://taiko.farewell.dev/images/rank_panel_Hard.webp"
        4 -> "https://taiko.farewell.dev/images/rank_panel_Oni.webp"
        5 -> "https://taiko.farewell.dev/images/rank_panel_Ura_Oni.webp"
        else -> "https://taiko.farewell.dev/images/rank_panel_Easy.webp"
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.65f)
            .clip(RoundedCornerShape(24.dp))
    ) {
        val cardWidth = maxWidth
        val cardHeight = maxHeight
        val fontSize = (cardHeight.value * 0.15f).sp
        val strokeWidth = cardHeight.value * 0.075f

        AsyncImage(
            model = panelUrl,
            contentDescription = "Rank Panel",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        @Composable
        fun NijiiroNumber(count: Int, xFactor: Float, yFactor: Float) {
            val textStr = count.toString()
            Box(
                modifier = Modifier.offset(
                    x = cardWidth * xFactor,
                    y = cardHeight * yFactor
                )
            ) {
                // Contour noir très épais et arrondi
                Text(
                    text = textStr,
                    style = TextStyle(
                        fontFamily = NijiiroFontFamily,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        drawStyle = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                )
                // Remplissage blanc
                Text(
                    text = textStr,
                    style = TextStyle(
                        fontFamily = NijiiroFontFamily,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }

        // Top Right: Kiwami (極)
        NijiiroNumber(stat.rankKiwamiCount, 0.80f, 0.14f)

        // Row 1: Miyabi Gold (5), Miyabi Pink (6), Miyabi Purple (7)
        NijiiroNumber(stat.rankMiyabiGoldCount, 0.22f, 0.33f)
        NijiiroNumber(stat.rankMiyabiPinkCount, 0.49f, 0.33f)
        NijiiroNumber(stat.rankMiyabiPurpleCount, 0.80f, 0.33f)

        // Row 2: Iki White (2), Iki Bronze (3), Iki Blue (4)
        NijiiroNumber(stat.rankIkiWhiteCount, 0.22f, 0.52f)
        NijiiroNumber(stat.rankIkiBronzeCount, 0.49f, 0.52f)
        NijiiroNumber(stat.rankIkiBlueCount, 0.80f, 0.52f)

        // Row 3: Silver Crown (Clear), Gold Crown (FC), Rainbow Crown (DFC)
        NijiiroNumber(stat.clearCount, 0.22f, 0.71f)
        NijiiroNumber(stat.fullComboCount, 0.49f, 0.71f)
        NijiiroNumber(stat.donderfulComboCount, 0.80f, 0.71f)
    }
}

@Composable
fun TotalProgressCard(overallStats: TaikoOverallProgressStats) {
    val totalSongs = overallStats.totalSongs
    val totalClear = overallStats.clearCount
    val totalFullCombo = overallStats.fullComboCount
    val totalDonderful = overallStats.donderfulComboCount

    val targetRate = if (totalSongs > 0) (totalClear.toFloat() / totalSongs) * 100f else 0f
    val animatedRate by animateFloatAsState(targetRate, tween(500), label = "totalRate")
    val totalRateFormatted = animatedRate.toDouble().format(2)

    val animatedTotalSongs by animateIntAsState(totalSongs, tween(500), label = "totalSongs")
    val animatedTotalClear by animateIntAsState(totalClear, tween(500), label = "totalClear")

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Progression au total",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$animatedTotalClear / $animatedTotalSongs chansons réussies",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$totalRateFormatted%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            val progressValue = if (totalSongs > 0) totalClear.toFloat() / totalSongs else 0f
            val animatedProgress by animateFloatAsState(progressValue, tween(500), label = "totalProgress")

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TotalStatBadge("Clear", totalClear, totalSongs, MaterialTheme.colorScheme.onSurface)
                TotalStatBadge("Full Combo", totalFullCombo, totalSongs, Color(0xFFD4A000))
                TotalStatBadge("Donderful", totalDonderful, totalSongs, Color(0xFFE91E63))
            }
        }
    }
}

@Composable
private fun TotalStatBadge(label: String, targetCount: Int, targetTotal: Int, color: Color) {
    val count by animateIntAsState(targetCount, tween(500), label = "badgeCount")
    val total by animateIntAsState(targetTotal, tween(500), label = "badgeTotal")
    
    val pct = if (total > 0) (count.toDouble() / total.toDouble()) * 100.0 else 0.0

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$count (${pct.format(1)}%)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun ProgressCard(
    stat: TaikoProgressStats,
    onShareClick: ((TaikoProgressStats) -> Unit)? = null,
    isNightMode: Boolean = isAppInDarkTheme()
) {
    val (difficultyName, difficultySubLabel) = when (stat.difficulty) {
        1 -> "Kantan" to "Facile"
        2 -> "Futsuu" to "Normal"
        3 -> "Muzukashii" to "Difficile"
        4 -> "Oni" to "Démoniaque"
        5 -> "Ura Oni" to "Ultra Démoniaque"
        else -> "Inconnu" to ""
    }

    val targetRate = stat.completionRate * 100f
    val animatedRate by animateFloatAsState(targetRate, tween(500), label = "cardRate")
    val rateFormatted = animatedRate.toDouble().format(1)

    val animatedClearCount by animateIntAsState(stat.clearCount, tween(500), label = "cardClearCount")
    val animatedTotalSongs by animateIntAsState(stat.totalSongs, tween(500), label = "cardTotalSongs")

    val textColor = if (isNightMode) Color.White else MaterialTheme.colorScheme.onSurface
    val subTextColor = if (isNightMode) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceBadgeBg = if (isNightMode) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val iconTint = if (isNightMode) Color.White else MaterialTheme.colorScheme.onSurface

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = setDifficultyColorBackground(stat.difficulty, isNightMode)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(getDifficultyDrawable(stat.difficulty)),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(90.dp)
                    .offset(x = 10.dp, y = 10.dp),
                contentScale = ContentScale.Fit,
                alpha = 0.22f
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Image(
                            painter = painterResource(getDifficultyDrawable(stat.difficulty)),
                            contentDescription = difficultyName,
                            modifier = Modifier.size(28.dp),
                            contentScale = ContentScale.Fit
                        )
                        Column {
                            Text(
                                text = difficultyName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (difficultySubLabel.isNotEmpty()) {
                                Text(
                                    text = difficultySubLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = subTextColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = surfaceBadgeBg
                        ) {
                            Text(
                                text = "$rateFormatted%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        if (onShareClick != null) {
                            IconButton(
                                onClick = { onShareClick.invoke(stat) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Partager la carte de rang",
                                    tint = iconTint,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "$animatedClearCount / $animatedTotalSongs chansons réussies",
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor
                )

                Spacer(modifier = Modifier.height(2.dp))

                ProgressRow(
                    label = "Clear",
                    targetCount = stat.clearCount,
                    targetTotal = stat.totalSongs,
                    barColor = if (isNightMode) Color(0xFFE0E0E0) else Color(0xFF616161),
                    isNightMode = isNightMode
                )
                ProgressRow(
                    label = "Full Combo",
                    targetCount = stat.fullComboCount,
                    targetTotal = stat.totalSongs,
                    barColor = if (isNightMode) Color(0xFFFFD700) else Color(0xFFD4A000),
                    isNightMode = isNightMode
                )
                ProgressRow(
                    label = "Donderful Combo",
                    targetCount = stat.donderfulComboCount,
                    targetTotal = stat.totalSongs,
                    barColor = if (isNightMode) Color(0xFFFF80AB) else Color(0xFFE91E63),
                    isNightMode = isNightMode
                )
            }
        }
    }
}

@Composable
fun ProgressRow(
    label: String,
    targetCount: Int,
    targetTotal: Int,
    barColor: Color = Color.White,
    isNightMode: Boolean = isAppInDarkTheme()
) {
    val count by animateIntAsState(targetCount, tween(500), label = "rowCount")
    val total by animateIntAsState(targetTotal, tween(500), label = "rowTotal")
    
    val targetProgress = if (targetTotal > 0) targetCount.toFloat() / targetTotal else 0f
    val animatedProgress by animateFloatAsState(targetProgress, tween(500), label = "rowProgress")
    
    val percentStr = if (total > 0) "${((count.toDouble() / total.toDouble()) * 100.0).format(1)}%" else "0.0%"

    val textColor = if (isNightMode) Color.White else MaterialTheme.colorScheme.onSurface
    val labelColor = if (isNightMode) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
    val trackColor = if (isNightMode) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$count ($percentStr)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = trackColor,
        )
    }
}

@Composable
fun RankPanelLegendCard(
    modifier: Modifier = Modifier,
    showTitle: Boolean = true
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (showTitle) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Légende de la carte",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Section 1: Rangs de Score (Paliers & Couleurs)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "RANGS DE SCORE (PALIERS NIJIIRO)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Kiwami
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AsyncImage(
                                model = "https://taiko.farewell.dev/images/rank_Dondaful.webp",
                                contentDescription = "Kiwami",
                                modifier = Modifier.height(20.dp),
                                contentScale = ContentScale.Fit
                            )
                            Text(
                                text = "極 Kiwami (Ultime)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "100% (1 000 000)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Miyabi
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "雅 Miyabi (Raffiné)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            RankSubItem(
                                modifier = Modifier.weight(1f),
                                imageUrl = "https://taiko.farewell.dev/images/rank_Gold.webp",
                                label = "Or: 80%",
                                subLabel = "800 000",
                                color = Color(0xFFD4A000)
                            )
                            RankSubItem(
                                modifier = Modifier.weight(1f),
                                imageUrl = "https://taiko.farewell.dev/images/rank_Sakura.webp",
                                label = "Rose: 90%",
                                subLabel = "900 000",
                                color = Color(0xFFE91E63)
                            )
                            RankSubItem(
                                modifier = Modifier.weight(1f),
                                imageUrl = "https://taiko.farewell.dev/images/rank_Purple.webp",
                                label = "Violet: 95%",
                                subLabel = "950 000",
                                color = Color(0xFF9C27B0)
                            )
                        }
                    }
                }

                // Iki
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "粋 Iki (Élégant)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            RankSubItem(
                                modifier = Modifier.weight(1f),
                                imageUrl = "https://taiko.farewell.dev/images/rank_White.webp",
                                label = "Blanc: 50%",
                                subLabel = "500 000",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            RankSubItem(
                                modifier = Modifier.weight(1f),
                                imageUrl = "https://taiko.farewell.dev/images/rank_Bronze.webp",
                                label = "Bronze: 60%",
                                subLabel = "600 000",
                                color = Color(0xFFCD7F32)
                            )
                            RankSubItem(
                                modifier = Modifier.weight(1f),
                                imageUrl = "https://taiko.farewell.dev/images/rank_Silver.webp",
                                label = "Bleu: 70%",
                                subLabel = "700 000",
                                color = Color(0xFF2196F3)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Section 2: Couronnes
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "COURONNES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LegendBadge(
                        modifier = Modifier.weight(1f),
                        imageUrl = "https://taiko.farewell.dev/images/crown_Clear.webp",
                        title = "Argent",
                        desc = "Clear (Réussi)"
                    )
                    LegendBadge(
                        modifier = Modifier.weight(1f),
                        imageUrl = "https://taiko.farewell.dev/images/crown_Gold.webp",
                        title = "Or",
                        desc = "Full Combo"
                    )
                    LegendBadge(
                        modifier = Modifier.weight(1f),
                        imageUrl = "https://taiko.farewell.dev/images/crown_Dondaful.webp",
                        title = "Arc-en-ciel",
                        desc = "100% Parfait"
                    )
                }
            }
        }
    }
}

@Composable
private fun RankSubItem(
    imageUrl: String,
    label: String,
    subLabel: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = label,
            modifier = Modifier.height(18.dp),
            contentScale = ContentScale.Fit
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LegendBadge(
    imageUrl: String?,
    title: String,
    desc: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        modifier = Modifier.height(18.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = desc,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
