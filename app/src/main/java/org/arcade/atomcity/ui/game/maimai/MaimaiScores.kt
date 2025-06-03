package org.arcade.atomcity.ui.game.maimai

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.presentation.viewmodel.MainActivityViewModel
import org.arcade.atomcity.ui.core.AchievementChip
import org.arcade.atomcity.ui.core.BottomBarPill
import org.arcade.atomcity.ui.core.OpenMiniMenu
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiScores(mainActivityViewModel: MainActivityViewModel) {

    val isLoading by mainActivityViewModel.isLoading.collectAsState()
    val data by mainActivityViewModel.data.collectAsState()

    var showMiniMenu by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableLongStateOf(0L) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(Unit) {
        mainActivityViewModel.fetchMaimaiPaginatedData(page = 1)
    }

    Scaffold (
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = "maimai") },
                navigationIcon = {},
                actions = {},
                scrollBehavior = scrollBehavior

            )
        },
        bottomBar = {
            BottomBarPill(
                currentPage = mainActivityViewModel.currentPage,
                onPageChange = { page ->
                    if (System.currentTimeMillis() - lastClickTime > 300) {
                        mainActivityViewModel.fetchMaimaiPaginatedData(page)
                        lastClickTime = System.currentTimeMillis()
                    }
                },
                onSettingsClick = {
                    if (System.currentTimeMillis() - lastClickTime > 300) {
                        Log.d("HomeScreen", "Settings clicked")
                        // Handle settings click
                        lastClickTime = System.currentTimeMillis()
                    }
                },
                onHomeClick = {
                    if (System.currentTimeMillis() - lastClickTime > 300) {
                        showMiniMenu = !showMiniMenu
                        lastClickTime = System.currentTimeMillis()
                    }
                }

            )
        },
) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(paddingValues)
                            .size(50.dp)
                            .align(Alignment.Center)
                    )
                }
            } else {
                LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                ) {
                    items(count = mainActivityViewModel.dataSize) { score ->
                        Card(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${data?.data?.get(score)?.song?.name?.en}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${data?.data?.get(score)?.song?.name?.jp}",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Text(
                                        text = "${data?.data?.get(score)?.song?.artist?.en} | ${
                                            data?.data?.get(
                                                score
                                            )?.song?.artist?.jp
                                        }",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = data?.data?.get(score)?.playDate?.let { dateString ->
                                                    val dateTime = LocalDateTime.parse(
                                                        // yeet the timezone
                                                        dateString.substring(0, 19),
                                                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                                                    )
                                                    val date = dateTime.format(
                                                        DateTimeFormatter.ofPattern("d MMMM yyyy")
                                                    )
                                                    val time = dateTime.format(
                                                        DateTimeFormatter.ofPattern("HH:mm")
                                                    )
                                                    "$date, $time"
                                                } ?: "",
                                                modifier = Modifier.align(Alignment.CenterStart),
                                                style = MaterialTheme.typography.bodySmall
                                            )

                                            AchievementChip(
                                                text = data?.data?.get(score)?.achievementFormatted
                                                    ?: "",
                                                modifier = Modifier.align(Alignment.CenterEnd)
                                            )
                                        }
                                    }

                                }
                            }
                        }
                    }
                }

            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 96.dp)
        ) {
            OpenMiniMenu(
                showMiniMenu = showMiniMenu,
                onDismiss = {
                    if (System.currentTimeMillis() - lastClickTime > 300) {
                        showMiniMenu = !showMiniMenu
                        lastClickTime = System.currentTimeMillis()
                    }
                },
                onItemClick = { Log.d("HomeScreen", "onItemClick") },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

    }
}





