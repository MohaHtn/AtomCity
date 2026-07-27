package org.arcade.atomcity.ui.game.maimai

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.arcade.atomcity.model.maitea.playsResponse.DifficultyLevel
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaApiData
import org.arcade.atomcity.model.maitea.playsResponse.Name
import org.arcade.atomcity.model.maitea.playsResponse.Song
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiBest30Charts(
    onBackClick: () -> Unit,
    navController: NavHostController,
    maiteaViewModel: MaiteaViewModel
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val isLoading by maiteaViewModel.isLoading.collectAsState()
    val maimaiBestScores by maiteaViewModel.maimaiBestScores.collectAsState()


    LaunchedEffect(Unit) {
        maiteaViewModel.fetch30BestScores()
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
    ) {
        paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
        {
            var i = 1;

            if (isLoading && maimaiBestScores.isEmpty()){
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else {
                Column() {
                    Text(modifier = Modifier.padding(32.dp), text = "Les scores affichés ci-dessous reflètent vos meilleurs scores parmi tous vos scores sur le jeu.", style = MaterialTheme.typography.bodyLarge)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        items(maimaiBestScores) { score ->
                            val jacketUrl = maiteaViewModel.findJacketUrlBySongName(score.songName)

                            // Mapping PlayerBest30Response to MaiteaApiData to reuse MaimaiScoreItem
                            val play = MaiteaApiData(
                                id = score.playId,
                                song = score.songJson,
                                achievementFormatted = String.format(LocalLocale.current.platformLocale, "%.2f%%", (score.achievement ?: 0.0) / 100.0),
                                rank = score.rank,
                                difficultyLevel = score.difficultyLevelJson,
                                rating = score.rating?.let { String.format(LocalLocale.current.platformLocale, "%.2f", it) },
                                playDate = score.playDate,
                                jacketImageUrl = jacketUrl,
                                isHighScore = false
                            )


                            MaimaiScoreItem(
                                play = play,
                                onClick = {
                                    navController.navigate("maimaiScoresDetails/${play.id}")
                                }
                            )
                            i += 1
                        }
                    }
                }

            }
        }
    }
}
