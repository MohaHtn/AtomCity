package org.arcade.atomcity.ui.game.maimai

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.arcade.atomcity.domain.repository.IDifficultyRepository
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherApiData
import org.arcade.atomcity.presentation.viewmodel.MaimaiViewModel
import org.arcade.atomcity.ui.navigation.navigateIfNotCurrent
import org.arcade.atomcity.utils.PlatformUtils
import org.arcade.atomcity.utils.format
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
fun MaimaiBest30Charts(
    onBackClick: () -> Unit,
    navController: NavHostController,
    maimaiViewModel: MaimaiViewModel,
    repository: IDifficultyRepository,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val isLoading by maimaiViewModel.isLoading.collectAsState()
    val maimaiBestScores by maimaiViewModel.maimaiBestScores.collectAsState()
    val playerData by maimaiViewModel.playerData.collectAsState()
    
    val scope = rememberCoroutineScope()
    val context = rememberPlatformContext()
    val graphicsLayer = rememberGraphicsLayer()
    var isGeneratingImage by remember { mutableStateOf(false) }
    var showSharePreview by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(false) }
    var isSaveAction by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        maimaiViewModel.fetch30BestScores()
        maimaiViewModel.fetchMaimaiPlayerDetails()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = "30 Meilleurs scores",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = maimaiBestScores.isNotEmpty() && !isGridView,
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && maimaiBestScores.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
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
                        val player = playerData?.data?.firstOrNull()
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
                                    shape = RoundedCornerShape(16.dp),
                                    tonalElevation = 2.dp,
                                    shadowElevation = 4.dp
                                ) {
                                    MaimaiBest30Summary(
                                        playerName = player?.name,
                                        rating = player?.rating,
                                        iconUrl = player?.options?.iconDeka?.webp ?: player?.options?.iconDeka?.png ?: player?.options?.icon?.webp ?: player?.options?.icon?.png,
                                        bannerUrl = player?.options?.frame?.webp ?: player?.options?.frame?.png,
                                        title = player?.options?.title?.value,
                                        scores = maimaiBestScores,
                                        repository = repository,
                                        isCapture = true
                                    )
                                }
                            }
                        }
                    } else {
                        Column {
                            Text(
                                modifier = Modifier.padding(32.dp),
                                text = "Les scores affichés ci-dessous reflètent vos meilleurs scores parmi tous vos scores sur le jeu.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(maimaiBestScores) { score ->
                                    val play = ScorefetcherApiData(
                                        id = score.playId,
                                        song = score.songJson,
                                        achievementFormatted = "${((score.achievement ?: 0.0) / 100.0).format(2)}%",
                                        rank = score.rank,
                                        difficultyLevel = score.difficultyLevelJson,
                                        rating = score.rating,
                                        playDate = score.playDate,
                                        jacketImageUrl = score.jacketImageUrl,
                                        isHighScore = false
                                    )

                                    MaimaiScoreItem(
                                        play = play,
                                        onClick = {
                                            navController.navigateIfNotCurrent("maimaiScoresDetails/${play.id}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Hidden capture area (composed for capturing B30 image)
            Box(
                modifier = Modifier
                    .wrapContentSize(align = Alignment.TopStart, unbounded = true)
                    .alpha(0.001f) // Kept in render tree for graphicsLayer recording
                    .drawWithContent {
                        if (isGeneratingImage) {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }
                        }
                    }
            ) {
                val player = playerData?.data?.firstOrNull()
                MaimaiBest30Summary(
                    playerName = player?.name,
                    rating = player?.rating,
                    iconUrl = player?.options?.iconDeka?.webp ?: player?.options?.iconDeka?.png ?: player?.options?.icon?.webp ?: player?.options?.icon?.png,
                    bannerUrl = player?.options?.frame?.webp ?: player?.options?.frame?.png,
                    title = player?.options?.title?.value,
                    scores = maimaiBestScores,
                    repository = repository,
                    modifier = Modifier.width(600.dp), // Slightly wider for better B30 look
                    isCapture = true
                )
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
                        PlatformUtils.log("MaimaiBest30Charts", "Error capturing or sharing B30 image: ${e.message}", true)
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
                            text = "Aperçu de vos 30 meilleurs scores",
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
                            val player = playerData?.data?.firstOrNull()
                            MaimaiBest30Summary(
                                playerName = player?.name,
                                rating = player?.rating,
                                iconUrl = player?.options?.iconDeka?.webp ?: player?.options?.iconDeka?.png ?: player?.options?.icon?.webp ?: player?.options?.icon?.png,
                                bannerUrl = player?.options?.frame?.webp ?: player?.options?.frame?.png,
                                title = player?.options?.title?.value,
                                scores = maimaiBestScores,
                                repository = repository,
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
                .pointerInput(containerWidth, containerHeight) {
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
