package org.arcade.atomcity.ui.game.maimai

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.presentation.viewmodel.MainActivityViewModel
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
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(Unit) {
        mainActivityViewModel.fetchData()
    }

    Scaffold (
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = "Atom City") },
                navigationIcon = {},
                actions = {},
                scrollBehavior = scrollBehavior

            )
        },
        bottomBar = {
            BottomBarPill(
                onHomeClick = {
                    if (System.currentTimeMillis() - lastClickTime > 300) {
                        showMiniMenu = !showMiniMenu
                        lastClickTime = System.currentTimeMillis()
                        Log.d("HomeScreen", "showMiniMenu = $showMiniMenu")

                    }
                }

            )
        }
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
                .padding(bottom = 96.dp) // Ajustez cette valeur selon vos besoins
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





@Composable
fun AchievementChip(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    shape: Shape = MaterialTheme.shapes.small,
    elevation: Dp = 4.dp
) {
    AssistChip(
        onClick = { /* Do something */ },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = backgroundColor,
            labelColor = contentColor
        ),
        shape = shape,
        elevation = AssistChipDefaults.assistChipElevation(elevation),
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    )
}

@Composable
fun BottomBarPill(
    onHomeClick: () -> Unit
){

    Box(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth())
    {
        Box(
            modifier = Modifier
                .height(40.dp)
                .align(Alignment.Center)
                .width(256.dp)
                .offset(y = (-64).dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick =  onHomeClick,
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(
                        text = "Games"
                    )
                }
                Button(
                    onClick = {  /* Do something */ },
                    modifier = Modifier.height(40.dp)

                ) {
                    Text(
                        text = "Settings"
                    )
                }
            }
        }
    }
}

@Composable
fun OpenMiniMenu(
    showMiniMenu: Boolean,
    onDismiss: () -> Unit,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = showMiniMenu,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier.width(512.dp).clickable(onClick = onDismiss).padding(16.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val items = listOf(
                        "maimai" to "les gros cerles là",
                        "beatmania IIDX" to "DJ ???? woa",
                        "pop n' music" to "miamme les burgers en forme de boutons"
                    )
                    items.forEach { (headline, supporting) ->
                        ListItem(
                            headlineContent = { Text(headline) },
                            supportingContent = { Text(supporting) },
                            modifier = Modifier.clickable { onItemClick() }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_8")
@Composable
fun BottomBarPillPreview() {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .offset(y = (64).dp)
    ) {
        BottomBarPill(
            onHomeClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OpenMiniMenuPreview() {
    Box(
        modifier = Modifier
            .padding(16.dp)
    ) {
        OpenMiniMenu(
            showMiniMenu = true,
            onDismiss = {},
            onItemClick = {},
        )
    }
}

@Preview(showBackground = false)
@Composable
fun AchievementChipPreview() {
    Box(
        modifier = Modifier
            .padding(16.dp)
    ) {
        AchievementChip(
            text = "98.12",
        )
    }
}