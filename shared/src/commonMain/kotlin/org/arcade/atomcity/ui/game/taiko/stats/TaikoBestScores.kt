package org.arcade.atomcity.ui.game.taiko.stats

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.game.taiko.TaikoScoreItem
import org.arcade.atomcity.utils.PlatformUtils
import org.arcade.atomcity.utils.rememberPlatformContext
import kotlin.time.Duration.Companion.milliseconds

private val GridViewIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "GridView",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(3f, 3f)
            lineTo(10.5f, 3f)
            lineTo(10.5f, 10.5f)
            lineTo(3f, 10.5f)
            close()
            moveTo(13.5f, 3f)
            lineTo(21f, 3f)
            lineTo(21f, 10.5f)
            lineTo(13.5f, 10.5f)
            close()
            moveTo(3f, 13.5f)
            lineTo(10.5f, 13.5f)
            lineTo(10.5f, 21f)
            lineTo(3f, 21f)
            close()
            moveTo(13.5f, 13.5f)
            lineTo(21f, 13.5f)
            lineTo(21f, 21f)
            lineTo(13.5f, 21f)
            close()
        }
    }.build()
}

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
            lineTo(19f, 18f)
            lineTo(19f, 20f)
            lineTo(5f, 20f)
            close()
        }
    }.build()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaikoBestScores(
    onBackClick: () -> Unit,
    navController: NavHostController,
    taikoViewModel: TaikoViewModel,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val bestScores by taikoViewModel.bestScores.collectAsState()
    val isLoading by taikoViewModel.isLoadingStats.collectAsState()
    val userSettings by taikoViewModel.userSettingsData.collectAsState()
    
    val scope = rememberCoroutineScope()
    val context = rememberPlatformContext()
    val graphicsLayer = rememberGraphicsLayer()
    var isGeneratingImage by remember { mutableStateOf(false) }
    var showSharePreview by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(false) }
    var isSaveAction by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        taikoViewModel.fetchBestScores()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Top 30 Scores",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Mes meilleurs scores absolus",
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
                actions = {
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Crossfade(
                            targetState = isGridView,
                            animationSpec = tween(250),
                            label = "IconSwitch"
                        ) { targetIsGrid ->
                            Icon(
                                imageVector = if (targetIsGrid) Icons.AutoMirrored.Filled.List else GridViewIcon,
                                contentDescription = if (targetIsGrid) "Vue liste" else "Vue grille"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = bestScores.isNotEmpty() && !isGridView,
                enter = fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.8f, animationSpec = tween(250)),
                exit = fadeOut(animationSpec = tween(250)) + scaleOut(targetScale = 0.8f, animationSpec = tween(250))
            ) {
                FloatingActionButton(
                    onClick = {
                        showSharePreview = true
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Prévisualiser le partage"
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (bestScores.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Aucun score trouvé",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Jouez quelques musiques pour voir vos meilleurs scores ici.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                AnimatedContent(
                    targetState = isGridView,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.92f, animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.92f, animationSpec = tween(300))
                    },
                    label = "ViewSwitchTransition"
                ) { targetIsGrid ->
                    if (targetIsGrid) {
                        ZoomableBox(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            AutoScaledGridBox(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                targetWidthDp = 600.dp
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    tonalElevation = 2.dp,
                                    shadowElevation = 4.dp
                                ) {
                                    TaikoBestScoresSummary(
                                        playerName = userSettings?.myDonName,
                                        scores = bestScores,
                                        isCapture = true
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                        ) {
                            itemsIndexed(bestScores) { index, entry ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val (rankBg, rankText) = when (index) {
                                        0 -> Color(0xFFFFD700) to "1"
                                        1 -> Color(0xFFC0C0C0) to "2"
                                        2 -> Color(0xFFCD7F32) to "3"
                                        else -> MaterialTheme.colorScheme.surfaceVariant to "${index + 1}"
                                    }
                                    val rankTextColor = when (index) {
                                        0, 1, 2 -> Color.Black
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = rankBg,
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .size(36.dp),
                                        shadowElevation = if (index < 3) 2.dp else 0.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "#$rankText",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = rankTextColor,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        TaikoScoreItem(
                                            score = entry,
                                            onNavigateToRoute = navController::navigate,
                                            onFavoriteToggle = {
                                                entry.songId?.let { taikoViewModel.toggleFavorite(it) }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Hidden capture area (only included during image generation to avoid touch interception)
            if (isGeneratingImage) {
                Box(
                    modifier = Modifier
                        .wrapContentSize(align = Alignment.TopStart, unbounded = true)
                        .drawWithContent {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }
                        }
                ) {
                    TaikoBestScoresSummary(
                        playerName = userSettings?.myDonName,
                        scores = bestScores,
                        modifier = Modifier.width(600.dp),
                        isCapture = true
                    )
                }
            }

            if (isGeneratingImage) {
                LaunchedEffect(isGeneratingImage) {
                    delay(600.milliseconds)
                    try {
                        val bitmap = graphicsLayer.toImageBitmap()
                        if (isSaveAction) {
                            PlatformUtils.saveImage(bitmap, context)
                        } else {
                            PlatformUtils.shareImage(bitmap, context)
                        }
                    } catch (e: Exception) {
                        PlatformUtils.log("TaikoBestScores", "Error capturing or sharing Taiko best scores image: ${e.message}", true)
                    } finally {
                        isGeneratingImage = false
                    }
                }
            }

            if (showSharePreview) {
                ModalBottomSheet(
                    onDismissRequest = { showSharePreview = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        Text(
                            text = "Aperçu de vos meilleurs scores",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1).sp
                            ),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .heightIn(max = 500.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp)
                        ) {
                            TaikoBestScoresSummary(
                                playerName = userSettings?.myDonName,
                                scores = bestScores,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        try {
                                            sheetState.hide()
                                        } catch (_: Exception) {
                                        } finally {
                                            showSharePreview = false
                                            isSaveAction = true
                                            isGeneratingImage = true
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(DownloadIcon, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Sauvegarder",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            sheetState.hide()
                                        } catch (_: Exception) {
                                        } finally {
                                            showSharePreview = false
                                            isSaveAction = false
                                            isGeneratingImage = true
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp),
                                shape = RoundedCornerShape(20.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Partager",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoScaledGridBox(
    modifier: Modifier = Modifier,
    targetWidthDp: Dp = 600.dp,
    content: @Composable () -> Unit
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val measurable = subcompose(Unit) { content() }.firstOrNull()

        if (measurable == null || constraints.maxWidth == 0 || constraints.maxHeight == 0) {
            return@SubcomposeLayout layout(constraints.minWidth, constraints.minHeight) {}
        }

        val targetWidthPx = targetWidthDp.roundToPx()

        val placeable = measurable.measure(
            Constraints(
                minWidth = targetWidthPx,
                maxWidth = targetWidthPx,
                minHeight = 0,
                maxHeight = Constraints.Infinity
            )
        )

        val contentWidth = placeable.width.toFloat()
        val contentHeight = placeable.height.toFloat()

        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()

        val scale = if (contentWidth > 0f && contentHeight > 0f) {
            minOf(containerWidth / contentWidth, containerHeight / contentHeight)
        } else {
            1f
        }

        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight

        layout(layoutWidth, layoutHeight) {
            val x = ((containerWidth - contentWidth) / 2f).toInt()
            val y = ((containerHeight - contentHeight) / 2f).toInt()

            placeable.placeWithLayer(x, y) {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
        }
    }
}

@Composable
private fun ZoomableBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = modifier.clipToBounds()) {
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()
        val containerCenter = Offset(containerWidth / 2f, containerHeight / 2f)

        val scaleAnim = remember { Animatable(1f) }
        val offsetXAnim = remember { Animatable(0f) }
        val offsetYAnim = remember { Animatable(0f) }
        val scope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(containerWidth, containerHeight) {
                    coroutineScope {
                        launch {
                            detectTransformGestures { centroid, pan, zoom, _ ->
                                val currentScale = scaleAnim.value
                                val currentOffset = Offset(offsetXAnim.value, offsetYAnim.value)

                                val newScale = (currentScale * zoom).coerceIn(1f, 5f)
                                val factor = if (currentScale > 0f) newScale / currentScale else 1f

                                val pivot = centroid - containerCenter

                                val newOffset = if (newScale > 1f) {
                                    val rawOffset = currentOffset * factor + pan - pivot * (factor - 1f)
                                    val maxOffsetX = (containerWidth * (newScale - 1f)) / 2f
                                    val maxOffsetY = (containerHeight * (newScale - 1f)) / 2f
                                    Offset(
                                        x = rawOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                                        y = rawOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
                                    )
                                } else {
                                    Offset.Zero
                                }

                                scope.launch {
                                    scaleAnim.snapTo(newScale)
                                    offsetXAnim.snapTo(newOffset.x)
                                    offsetYAnim.snapTo(newOffset.y)
                                }
                            }
                        }

                        launch {
                            detectTapGestures(
                                onDoubleTap = { tapOffset ->
                                    scope.launch {
                                        if (scaleAnim.value > 1.1f) {
                                            launch { scaleAnim.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow)) }
                                            launch { offsetXAnim.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow)) }
                                            launch { offsetYAnim.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow)) }
                                        } else {
                                            val targetScale = 2.5f
                                            val maxOffsetX = (containerWidth * (targetScale - 1f)) / 2f
                                            val maxOffsetY = (containerHeight * (targetScale - 1f)) / 2f

                                            val pivot = tapOffset - containerCenter
                                            val targetOffset = (-pivot * (targetScale - 1f)).let {
                                                Offset(
                                                    x = it.x.coerceIn(-maxOffsetX, maxOffsetX),
                                                    y = it.y.coerceIn(-maxOffsetY, maxOffsetY)
                                                )
                                            }

                                            launch { scaleAnim.animateTo(targetScale, spring(stiffness = Spring.StiffnessMediumLow)) }
                                            launch { offsetXAnim.animateTo(targetOffset.x, spring(stiffness = Spring.StiffnessMediumLow)) }
                                            launch { offsetYAnim.animateTo(targetOffset.y, spring(stiffness = Spring.StiffnessMediumLow)) }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    translationX = offsetXAnim.value
                    translationY = offsetYAnim.value
                }
        ) {
            content()
        }
    }
}
