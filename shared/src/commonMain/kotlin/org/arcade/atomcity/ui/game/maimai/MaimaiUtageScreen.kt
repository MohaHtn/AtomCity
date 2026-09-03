package org.arcade.atomcity.ui.game.maimai

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
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.arcade.atomcity.presentation.viewmodel.MaimaiViewModel
import org.arcade.atomcity.ui.game.maimai.utage.UtageExpressiveItem
import org.arcade.atomcity.ui.game.maimai.utage.UtageInfoBottomSheet
import org.arcade.atomcity.ui.game.maimai.utage.UtageSearchBar
import org.arcade.atomcity.ui.game.maimai.utage.mergeUtageData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiUtageScreen(
    onBackClick: () -> Unit,
    viewModel: MaimaiViewModel,
    navController: NavController
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val isLoading by viewModel.isLoadingUtageScores.collectAsState()
    val utageScores by viewModel.maimaiUtageScores.collectAsState()
    val utageStaticData by viewModel.utageStaticData.collectAsState()
    
    var showInfoSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchUtageScores()
        if (utageStaticData == null) {
            viewModel.fetchUtageStaticData()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = "Utage",
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
                    IconButton(onClick = { showInfoSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Informations"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        var searchQuery by remember { mutableStateOf("") }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                UtageSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (isLoading && utageScores.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (!isLoading && utageScores.isEmpty() && utageStaticData == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Aucun score Utage trouvé.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    val mergedItems = remember(utageScores, utageStaticData) {
                        mergeUtageData(utageScores, utageStaticData)
                    }

                    val filteredItems = remember(mergedItems, searchQuery) {
                        if (searchQuery.isBlank()) {
                            mergedItems
                        } else {
                            val q = searchQuery.trim().lowercase()
                            mergedItems.filter { item ->
                                item.songTitle.lowercase().contains(q) ||
                                item.attribute?.lowercase()?.contains(q) == true ||
                                item.details?.lowercase()?.contains(q) == true
                            }
                        }
                    }

                    if (filteredItems.isEmpty() && searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aucune Utage trouvée pour \"$searchQuery\".",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            items(filteredItems) { item ->
                                UtageExpressiveItem(
                                    item = item,
                                    navController = navController,
                                    utageData = utageStaticData
                                )
                            }
                        }
                    }
                }
            }

            if (showInfoSheet) {
                UtageInfoBottomSheet(
                    onDismiss = { showInfoSheet = false },
                    utageData = utageStaticData
                )
            }
        }
    }
}
