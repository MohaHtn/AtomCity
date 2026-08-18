package org.arcade.atomcity.ui.game.maimai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.arcade.atomcity.domain.repository.IDifficultyRepository
import org.arcade.atomcity.data.remote.model.scorefetcher.playerBest30Response.PlayerBest30Response
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherApiData
import org.arcade.atomcity.presentation.viewmodel.ScorefetcherViewModel
import org.arcade.atomcity.utils.PlatformUtils
import org.arcade.atomcity.utils.format
import org.arcade.atomcity.utils.rememberPlatformContext
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiBest30Charts(
    onBackClick: () -> Unit,
    navController: NavHostController,
    scorefetcherViewModel: ScorefetcherViewModel,
    repository: IDifficultyRepository,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val isLoading by scorefetcherViewModel.isLoading.collectAsState()
    val maimaiBestScores by scorefetcherViewModel.maimaiBestScores.collectAsState()
    val playerData by scorefetcherViewModel.playerData.collectAsState()
    
    val scope = rememberCoroutineScope()
    val context = rememberPlatformContext()
    val graphicsLayer = rememberGraphicsLayer()
    var isGeneratingImage by remember { mutableStateOf(false) }
    var showSharePreview by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    LaunchedEffect(Unit) {
        scorefetcherViewModel.fetch30BestScores()
        scorefetcherViewModel.fetchMaimaiPlayerDetails()
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
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (maimaiBestScores.isNotEmpty()) {
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
    ) {
        paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
        {
            if (isLoading && maimaiBestScores.isEmpty()){
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else {
                Column {
                    Text(modifier = Modifier.padding(32.dp), text = "Les scores affichés ci-dessous reflètent vos meilleurs scores parmi tous vos scores sur le jeu.", style = MaterialTheme.typography.bodyLarge)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        items(maimaiBestScores) { score ->
                            // Mapping PlayerBest30Response to ScorefetcherApiData to reuse MaimaiScoreItem
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
                                    navController.navigate("maimaiScoresDetails/${play.id}")
                                }
                            )
                        }
                    }
                }
            }
            
            // Hidden capture area (composed but invisible to pre-load images)
            Box(
                modifier = Modifier
                    .wrapContentSize(align = Alignment.TopStart, unbounded = true)
                    .alpha(0f) // Invisible
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
                LaunchedEffect(Unit) {
                    // Larger delay to ensure images are loaded
                    delay(1000.milliseconds)
                    scope.launch {
                        try {
                            val bitmap = graphicsLayer.toImageBitmap()
                            PlatformUtils.shareImage(bitmap, context)
                        } catch (e: Exception) {
                            // Handle error
                        } finally {
                            isGeneratingImage = false
                        }
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
                            .navigationBarsPadding() // Correct way to handle bottom nav bar
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
                                .heightIn(max = 500.dp) // Limit height of preview to keep button visible
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
                                repository  = repository,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    sheetState.hide()
                                    showSharePreview = false
                                    isGeneratingImage = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                                .height(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Partager",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
