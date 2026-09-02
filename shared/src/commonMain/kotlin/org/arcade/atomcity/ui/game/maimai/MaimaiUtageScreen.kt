package org.arcade.atomcity.ui.game.maimai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.arcade.atomcity.data.remote.model.maimai.*
import org.arcade.atomcity.data.remote.model.scorefetcher.playerBest30Response.PlayerBest30Response
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherApiData
import org.arcade.atomcity.presentation.viewmodel.MaimaiViewModel
import org.arcade.atomcity.utils.format
import coil3.compose.AsyncImage

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

@Composable
fun UtageSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Rechercher une Utage...",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Effacer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

enum class UtageAttributeCategory(val displayName: String) {
    PRE_DX("Pre-DX"),
    POST_DX("Post-DX"),
    OFF("Spéciaux"),
    UNUSED("Inutilisés")
}

data class CategorizedUtageAttribute(
    val attribute: UtageAttribute,
    val category: UtageAttributeCategory,
    val categoryName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtageInfoBottomSheet(
    onDismiss: () -> Unit,
    utageData: UtageData?
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val categoryDescriptions = remember(utageData) {
        val map = mutableMapOf<UtageAttributeCategory, String>()
        fun checkAndExtract(list: List<UtageAttribute>, cat: UtageAttributeCategory) {
            val descObjs = list.filter { it.attribute.isNullOrBlank() }
            val texts = descObjs.mapNotNull {
                (it.description ?: it.description_fr ?: it.details_fr ?: it.comments_fr)?.trim()
            }.filter { it.isNotEmpty() }
            if (texts.isNotEmpty()) {
                map[cat] = texts.joinToString("\n\n")
            }
        }
        utageData?.chart_attributes?.let { attrs ->
            checkAndExtract(attrs.pre_dx, UtageAttributeCategory.PRE_DX)
            checkAndExtract(attrs.post_dx, UtageAttributeCategory.POST_DX)
            checkAndExtract(attrs.off_attributes, UtageAttributeCategory.OFF)
            checkAndExtract(attrs.unused_attributes, UtageAttributeCategory.UNUSED)
        }
        map
    }

    val allCategorizedAttributes = remember(utageData) {
        val list = mutableListOf<CategorizedUtageAttribute>()
        utageData?.chart_attributes?.let { attrs ->
            attrs.pre_dx.filter { !it.attribute.isNullOrBlank() }.forEach {
                list.add(CategorizedUtageAttribute(it, UtageAttributeCategory.PRE_DX, "Pre-DX"))
            }
            attrs.post_dx.filter { !it.attribute.isNullOrBlank() }.forEach {
                list.add(CategorizedUtageAttribute(it, UtageAttributeCategory.POST_DX, "Post-DX"))
            }
            attrs.off_attributes.filter { !it.attribute.isNullOrBlank() }.forEach {
                list.add(CategorizedUtageAttribute(it, UtageAttributeCategory.OFF, "Spéciaux"))
            }
            attrs.unused_attributes.filter { !it.attribute.isNullOrBlank() }.forEach {
                list.add(CategorizedUtageAttribute(it, UtageAttributeCategory.UNUSED, "Inutilisés"))
            }
        }
        list
    }

    var selectedCategory by remember { mutableStateOf(UtageAttributeCategory.PRE_DX) }

    val preDxCount = remember(allCategorizedAttributes) { allCategorizedAttributes.count { it.category == UtageAttributeCategory.PRE_DX } }
    val postDxCount = remember(allCategorizedAttributes) { allCategorizedAttributes.count { it.category == UtageAttributeCategory.POST_DX } }
    val offCount = remember(allCategorizedAttributes) { allCategorizedAttributes.count { it.category == UtageAttributeCategory.OFF } }
    val unusedCount = remember(allCategorizedAttributes) { allCategorizedAttributes.count { it.category == UtageAttributeCategory.UNUSED } }

    val filteredAttributes = remember(allCategorizedAttributes, selectedCategory) {
        allCategorizedAttributes.filter { it.category == selectedCategory }
    }

    val currentCategoryDescription = categoryDescriptions[selectedCategory]

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            // En-tête de la sheet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Attributs des charts Utage",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Guide des spécificités des charts 宴 (Utage)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (utageData == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Chargement des attributs...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Onglets / Filtres de catégories
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(UtageAttributeCategory.entries.toTypedArray()) { cat ->
                        val isSelected = selectedCategory == cat
                        val count = when (cat) {
                            UtageAttributeCategory.PRE_DX -> preDxCount
                            UtageAttributeCategory.POST_DX -> postDxCount
                            UtageAttributeCategory.OFF -> offCount
                            UtageAttributeCategory.UNUSED -> unusedCount
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = cat.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "$count",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = Color.Transparent,
                                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Liste d'attributs
                if (filteredAttributes.isEmpty() && currentCategoryDescription.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucun attribut dans cette catégorie.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!currentCategoryDescription.isNullOrBlank()) {
                            item(key = "desc_${selectedCategory.name}") {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = currentCategoryDescription,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                lineHeight = 20.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        items(
                            items = filteredAttributes,
                            key = { "attr_${it.category.name}_${it.attribute.attribute}" }
                        ) { catAttr ->
                            ExpressiveAttributeCard(item = catAttr)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpressiveAttributeCard(
    item: CategorizedUtageAttribute,
    modifier: Modifier = Modifier
) {
    val attr = item.attribute
    val detailText = attr.details_fr ?: attr.comments_fr ?: attr.description ?: ""

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                if (attr.img != null) {
                    AsyncImage(
                        model = attr.img,
                        contentDescription = attr.attribute,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )
                } else {
                    Text(
                        text = attr.attribute?.take(1) ?: "宴",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attr.attribute ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (detailText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = detailText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 20.sp,
                            letterSpacing = 0.15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class UtageDisplayItem(
    val songTitle: String,
    val attribute: String?,
    val details: String?,
    val comment: String? = null,
    val forcedOptions: String? = null,
    val score: PlayerBest30Response? = null
)

fun mergeUtageData(
    scores: List<PlayerBest30Response>,
    staticData: UtageData?
): List<UtageDisplayItem> {
    if (staticData == null) return scores.map { score ->
        UtageDisplayItem(
            songTitle = score.songJson?.name?.jp ?: score.songJson?.name?.en ?: "Unknown",
            attribute = score.difficultyLevelJson?.label,
            details = null,
            comment = null,
            forcedOptions = null,
            score = score
        )
    }

    val results = mutableListOf<UtageDisplayItem>()
    val matchedScoreIds = mutableSetOf<Int>()

    staticData.utage_chart_list.forEach { chartEntry ->
        if (chartEntry.variants != null) {
            chartEntry.variants.forEach { variant ->
                val matchingScore = findMatchingScore(chartEntry.song, variant.attribute, scores, matchedScoreIds)
                results.add(UtageDisplayItem(
                    songTitle = chartEntry.song,
                    attribute = variant.attribute,
                    details = variant.details_fr ?: chartEntry.details_fr,
                    comment = variant.comment ?: chartEntry.comment,
                    forcedOptions = variant.forced_options ?: chartEntry.forced_options,
                    score = matchingScore
                ))
                matchingScore?.playId?.let { matchedScoreIds.add(it) }
            }
        } else {
            val matchingScore = findMatchingScore(chartEntry.song, chartEntry.attribute, scores, matchedScoreIds)
            results.add(UtageDisplayItem(
                songTitle = chartEntry.song,
                attribute = chartEntry.attribute,
                details = chartEntry.details_fr,
                comment = chartEntry.comment,
                forcedOptions = chartEntry.forced_options,
                score = matchingScore
            ))
            matchingScore?.playId?.let { matchedScoreIds.add(it) }
        }
    }

    // Ajouter les scores restants (ceux non trouvés dans le JSON)
    scores.filter { it.playId != null && it.playId !in matchedScoreIds }.forEach { score ->
        results.add(UtageDisplayItem(
            songTitle = score.songJson?.name?.jp ?: score.songJson?.name?.en ?: "Unknown",
            attribute = score.difficultyLevelJson?.label,
            details = null,
            comment = null,
            forcedOptions = null,
            score = score
        ))
    }

    // Trier par achievement décroissant (les scores null iront à la fin)
    return results.sortedWith(
        compareByDescending<UtageDisplayItem> { it.score?.achievement ?: -1.0 }
            .thenBy { it.songTitle }
    )
}

fun findMatchingScore(
    jsonSong: String, 
    jsonAttribute: String?, 
    scores: List<PlayerBest30Response>,
    usedIds: Set<Int>
): PlayerBest30Response? {
    val normFullJson = normalizeString(jsonSong)
    
    // Découpage bilingue uniquement s'il y a un tiret avec espaces (ex: "ジンギスカン - Genghis Khan")
    val jsonParts = if (jsonSong.contains(" - ") || jsonSong.contains(" -") || jsonSong.contains("- ")) {
        jsonSong.split(Regex("\\s*-\\s*"))
    } else {
        listOf(jsonSong)
    }
    
    val jsonJpPart = jsonParts.getOrNull(0)?.trim() ?: jsonSong
    val jsonEnPart = jsonParts.getOrNull(1)?.trim()
    
    val normJsonJp = normalizeString(jsonJpPart)
    val normJsonEn = jsonEnPart?.let { normalizeString(it) }
    
    val rawJsonAttr = jsonAttribute?.replace("(", "")?.replace(")", "")?.trim() ?: ""

    // Passe 1 : Match EXACT sur le titre complet ou les parties JP/EN
    val exactMatch = scores.find { score ->
        if (score.playId != null && score.playId in usedIds) return@find false

        val apiJp = normalizeString(score.songJson?.name?.jp)
        val apiEn = normalizeString(score.songJson?.name?.en)
        
        val isExact = (normFullJson.isNotEmpty() && (normFullJson == apiJp || normFullJson == apiEn)) ||
                      (normJsonJp.isNotEmpty() && (normJsonJp == apiJp || normJsonJp == apiEn)) ||
                      (!normJsonEn.isNullOrEmpty() && (normJsonEn == apiEn || normJsonEn == apiJp))

        if (!isExact) return@find false

        val apiAttr = score.difficultyLevelJson?.label?.replace("(", "")?.replace(")", "")?.trim() ?: ""
        
        when {
            rawJsonAttr.isNotEmpty() && apiAttr.isNotEmpty() && rawJsonAttr == apiAttr -> true
            apiAttr == "宴" || apiAttr.isEmpty() -> true
            rawJsonAttr == "宴" || rawJsonAttr.isEmpty() -> true
            else -> true
        }
    }

    if (exactMatch != null) return exactMatch

    // Passe 2 : Match par sous-chaîne (uniquement pour les termes de plus de 3 caractères pour éviter que "M" matche tout)
    return scores.find { score ->
        if (score.playId != null && score.playId in usedIds) return@find false

        val apiJp = normalizeString(score.songJson?.name?.jp)
        val apiEn = normalizeString(score.songJson?.name?.en)

        val isContainsJp = normJsonJp.length > 3 && apiJp.isNotEmpty() && (apiJp.contains(normJsonJp) || normJsonJp.contains(apiJp))
        val isContainsEn = !normJsonEn.isNullOrEmpty() && normJsonEn.length > 3 && apiEn.isNotEmpty() && (apiEn.contains(normJsonEn) || normJsonEn.contains(apiEn))

        if (!isContainsJp && !isContainsEn) return@find false

        val apiAttr = score.difficultyLevelJson?.label?.replace("(", "")?.replace(")", "")?.trim() ?: ""
        
        when {
            rawJsonAttr.isNotEmpty() && apiAttr.isNotEmpty() && rawJsonAttr == apiAttr -> true
            apiAttr == "宴" || apiAttr.isEmpty() -> true
            rawJsonAttr == "宴" || rawJsonAttr.isEmpty() -> true
            else -> true
        }
    }
}

@Composable
fun UtageExpressiveItem(
    item: UtageDisplayItem,
    navController: NavController,
    utageData: UtageData? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val attrInfo = remember(item.attribute, utageData) {
        findUtageAttributeInfo(item.attribute, utageData)
    }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Carte principale de Score ou Carte Vide
        if (item.score != null) {
            val play = ScorefetcherApiData(
                id = item.score.playId,
                song = item.score.songJson,
                achievementFormatted = "${((item.score.achievement ?: 0.0) / 100.0).format(2)}%",
                rank = item.score.rank,
                difficultyLevel = item.score.difficultyLevelJson,
                rating = item.score.rating,
                playDate = item.score.playDate,
                jacketImageUrl = item.score.jacketImageUrl,
                isHighScore = false
            )

            MaimaiScoreItem(
                play = play,
                onClick = {
                    navController.navigate("maimaiScoresDetails/${play.id}")
                },
                footer = {
                    if (item.details != null) {
                        IconButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier
                                .size(28.dp)
                                .offset(y = (-14).dp, x = 4.dp)
                                .background(
                                    color = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.Star else Icons.Default.Info,
                                contentDescription = "Guide",
                                modifier = Modifier.size(16.dp),
                                tint = if (expanded) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        } else {
            EmptyUtageCard(
                title = item.songTitle, 
                attribute = item.attribute,
                hasDetails = item.details != null,
                isExpanded = expanded,
                onExpandClick = { expanded = !expanded }
            )
        }

        // Contenu du guide déroulant
        AnimatedVisibility(
            visible = expanded && item.details != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            if (item.details != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 24.dp,
                        bottomEnd = 24.dp,
                        bottomStart = 24.dp
                    ),
                    modifier = Modifier
                        .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                        .fillMaxWidth()
                        .clickable { expanded = false }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        if (attrInfo?.img != null) {
                            AsyncImage(
                                model = attrInfo.img,
                                contentDescription = attrInfo.attribute,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(2.dp)
                            )
                        } else {
                            Surface(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = item.attribute ?: "宴",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            val displayAttrTitle = attrInfo?.attribute ?: item.attribute
                            if (!displayAttrTitle.isNullOrBlank()) {
                                Text(
                                    text = displayAttrTitle,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            val parsedComments = remember(item.comment) {
                                parseUtageComment(item.comment)
                            }

                            if (parsedComments.isNotEmpty()) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                                    ),
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomEnd = 16.dp,
                                        bottomStart = 4.dp
                                    ),
                                    modifier = Modifier
                                        .padding(bottom = 10.dp)
                                        .fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(IntrinsicSize.Min)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .fillMaxHeight()
                                                .background(
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 4.dp)
                                                )
                                        )

                                        Column(
                                            modifier = Modifier
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                                .weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            parsedComments.forEachIndexed { index, eraComment ->
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    if (eraComment.jp.isNotEmpty()) {
                                                        Text(
                                                            text = if (parsedComments.size > 1) "• ${eraComment.jp}" else eraComment.jp,
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                lineHeight = 18.sp
                                                            ),
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                                        )
                                                    }
                                                    if (!eraComment.en.isNullOrBlank()) {
                                                        Text(
                                                            text = if (parsedComments.size > 1) "  ${eraComment.en}" else eraComment.en,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                lineHeight = 16.sp
                                                            ),
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                                        )
                                                    }
                                                }

                                                if (index < parsedComments.size - 1) {
                                                    HorizontalDivider(
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.15f),
                                                        thickness = 0.5.dp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Text(
                                text = item.details,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 22.sp,
                                    letterSpacing = 0.25.sp
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            val parsedForcedOptions = remember(item.forcedOptions) {
                                parseForcedOptions(item.forcedOptions)
                            }

                            if (parsedForcedOptions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f),
                                    thickness = 1.dp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Options forcées :",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    parsedForcedOptions.forEach { opt ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "• ${opt.optionName} : ",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                            )
                                            Text(
                                                text = opt.value,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary
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
    }
}

val FORCED_OPTIONS_LEGEND = listOf(
    "Vitesse des notes",
    "Mode miroir",
    "Infos en fond",
    "Saut de piste",
    "Style de jugement"
)

data class ForcedOptionInfo(
    val optionName: String,
    val value: String
)

fun parseForcedOptions(forcedOptionsStr: String?): List<ForcedOptionInfo> {
    if (forcedOptionsStr.isNullOrBlank()) return emptyList()
    val parts = forcedOptionsStr.split("/").map { it.trim() }
    
    val result = mutableListOf<ForcedOptionInfo>()
    for (i in parts.indices) {
        val valStr = parts[i]
        if (valStr != "-" && valStr.isNotEmpty() && i < FORCED_OPTIONS_LEGEND.size) {
            result.add(ForcedOptionInfo(FORCED_OPTIONS_LEGEND[i], valStr))
        }
    }
    return result
}

fun findUtageAttributeInfo(attributeKey: String?, staticData: UtageData?): UtageAttribute? {
    if (attributeKey.isNullOrBlank() || staticData?.chart_attributes == null) return null
    val key = attributeKey.replace("(", "").replace(")", "").trim()
    if (key.isEmpty()) return null

    val allAttributes = staticData.chart_attributes.pre_dx + 
                         staticData.chart_attributes.post_dx + 
                         staticData.chart_attributes.off_attributes + 
                         staticData.chart_attributes.unused_attributes

    // 1. Recherche directe (ex: "宴" ou "光")
    val directMatch = allAttributes.find { attr ->
        val attrName = attr.attribute ?: ""
        attrName.startsWith(key) || attrName.contains(key)
    }
    if (directMatch != null) return directMatch

    // 2. Si la clé contient "宴" combiné à un autre caractère (ex: "宴 (星)"), on nettoie "宴"
    if (key.length > 1 && key.contains("宴")) {
        val cleanKey = key.replace("宴", "").trim()
        if (cleanKey.isNotEmpty()) {
            return allAttributes.find { attr ->
                val attrName = attr.attribute ?: ""
                attrName.startsWith(cleanKey) || attrName.contains(cleanKey)
            }
        }
    }

    return null
}

@Composable
fun EmptyUtageCard(
    title: String, 
    attribute: String?,
    hasDetails: Boolean = false,
    isExpanded: Boolean = false,
    onExpandClick: () -> Unit = {}
) {
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .padding(horizontal = 2.dp)
            .drawBehind {
                val stroke = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
                )
                drawRoundRect(
                    color = outlineColor,
                    style = stroke,
                    cornerRadius = CornerRadius(24.dp.toPx())
                )
            }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(outlineColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = attribute ?: "宴",
                    style = MaterialTheme.typography.headlineSmall,
                    color = outlineColor,
                    fontWeight = FontWeight.Black
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "Score non disponible",
                    style = MaterialTheme.typography.labelMedium,
                    color = outlineColor
                )
            }

            if (hasDetails) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onExpandClick,
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.Star else Icons.Default.Info,
                        contentDescription = "Guide",
                        modifier = Modifier.size(16.dp),
                        tint = if (isExpanded) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

fun normalizeString(s: String?): String = s?.lowercase()
    ?.replace(Regex("[^a-z0-9\u3040-\u309f\u30a0-\u30ff\u4e00-\u9faf\u4e00-\u9fff]"), "")
    ?.trim() ?: ""

data class EraComment(
    val jp: String,
    val en: String? = null
)

fun parseUtageComment(rawCommentStr: String?): List<EraComment> {
    if (rawCommentStr.isNullOrBlank()) return emptyList()
    val raw = rawCommentStr.trim()

    val hasDash = raw.contains(" - ") || raw.contains(" — ") || raw.contains(" -") || raw.contains("- ")
    val parts = if (hasDash) raw.split(Regex("\\s+[-—]\\s+"), limit = 2) else listOf(raw)

    val jpRaw = parts.getOrNull(0)?.trim() ?: ""
    val enRaw = parts.getOrNull(1)?.trim()

    // Permet de découper même quand le slash n'a pas d'espace juste après (ex: /Jack...)
    val jpList = jpRaw.split(Regex("\\s*/\\s*")).map { it.trim() }.filter { it.isNotEmpty() }
    val enList = enRaw?.split(Regex("\\s*/\\s*"))?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

    val result = mutableListOf<EraComment>()
    val maxLen = maxOf(jpList.size, enList.size)

    for (i in 0 until maxLen) {
        val jpStr = jpList.getOrNull(i) ?: ""
        val enStr = enList.getOrNull(i)
        
        if (jpStr.isNotEmpty() && !enStr.isNullOrEmpty() && jpStr != enStr) {
            result.add(EraComment(jp = jpStr, en = enStr))
        } else if (jpStr.isNotEmpty()) {
            result.add(EraComment(jp = jpStr, en = null))
        } else if (!enStr.isNullOrEmpty()) {
            result.add(EraComment(jp = enStr, en = null))
        }
    }

    return result
}
