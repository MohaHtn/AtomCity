package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.FilterQuality
import coil3.compose.AsyncImage
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import org.arcade.atomcity.data.remote.model.taikoserver.TaikoImagesData
import org.arcade.atomcity.data.remote.model.taikoserver.gamedata.TaikoServerCostume
import org.arcade.atomcity.data.remote.model.taikoserver.gamedata.TaikoServerTitlesResponse
import org.arcade.atomcity.data.remote.model.taikoserver.usersettings.TaikoServerUserSettingsResponse
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaikoUserSettings(
    taikoViewModel: TaikoViewModel,
    onBackClick: () -> Unit
) {
    val serverSettings by taikoViewModel.userDetailedSettings.collectAsState()
    val costumes by taikoViewModel.costumesData.collectAsState()
    val titles by taikoViewModel.titlesData.collectAsState()
    val imagesData by taikoViewModel.imagesData.collectAsState()
    val isLoading by taikoViewModel.isLoadingUserSettings.collectAsState()
    
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    var localSettings by remember(serverSettings) { mutableStateOf(serverSettings) }
    var isSaving by remember { mutableStateOf(false) }

    var showDialogFor by remember { mutableStateOf<String?>(null) }
    var showTitleSearchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        taikoViewModel.fetchImagesData()
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = RoundedCornerShape(24.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { taikoViewModel.userSettingsData.value?.myDonName?.let {
                    Text(
                        text = it,
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                ) }},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        floatingActionButton = {
            if (localSettings != serverSettings) {
                ExtendedFloatingActionButton(
                    onClick = {
                        localSettings?.let {
                            scope.launch {
                                isSaving = true
                                val success = taikoViewModel.updateUserSettings(it)
                                isSaving = false
                                if (success) {
                                    onBackClick()
                                    taikoViewModel.showSnackbar("Paramètres enregistrés.")
                                } else {
                                    snackbarHostState.showSnackbar("Erreur lors de l'enregistrement.")
                                }
                            }
                        }
                    },
                    icon = {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimaryContainer, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    },
                    text = { Text("Enregistrer") },
                    expanded = !isSaving,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            localSettings?.let { data ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val nameplateUrls = taikoViewModel.getNameplateUrls(data)
                    ProfileHeader(data, imagesData, nameplateUrls)
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    SettingSection("Profil") {
                        OutlinedTextField(
                            value = data.myDonName ?: "",
                            onValueChange = { localSettings = data.copy(myDonName = it) },
                            label = { Text("Nom") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(50),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = data.title ?: "",
                            onValueChange = { localSettings = data.copy(title = it) },
                            label = { Text("Titre") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            shape = RoundedCornerShape(50),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { showTitleSearchDialog = true }) {
                                    Icon(Icons.Default.Search, contentDescription = "Rechercher un titre")
                                }
                            }
                        )

                        val titleName = titles?.get(data.titlePlateId.toString())?.titleNameEN ?: data.title ?: "Inconnu"
                        ExpressiveGridItem(
                            label = "Plaque de titre",
                            value = titleName,
                            imageUrl = buildImageUrl("title", findPlateFilename(data.titlePlateId, imagesData)),
                            onClick = { showDialogFor = "title" },
                            modifier = Modifier.fillMaxWidth().height(120.dp)
                        )

                        SettingToggle("Montrer le Dan sur la nameplate", data.isDisplayDanOnNamePlate ?: false) {
                            localSettings = data.copy(isDisplayDanOnNamePlate = it)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingSection("Costume") {
                        val kigurumiName = costumes?.find { it.costumeId == data.kigurumi && it.costumeType == "kigurumi" }?.costumeNameEN?.let { if (it == "Plain Don") "Défaut" else it } ?: "ID: ${data.kigurumi}"
                        val headName = costumes?.find { it.costumeId == data.head && it.costumeType == "head" }?.costumeNameEN?.let { if (it == "Plain Don") "Défaut" else it } ?: "ID: ${data.head}"
                        val bodyName = costumes?.find { it.costumeId == data.body && it.costumeType == "body" }?.costumeNameEN?.let { if (it == "Plain Don") "Défaut" else it } ?: "ID: ${data.body}"
                        val faceName = costumes?.find { it.costumeId == data.face && it.costumeType == "face" }?.costumeNameEN?.let { if (it == "Plain Don") "Défaut" else it } ?: "ID: ${data.face}"
                        val puchiName = costumes?.find { it.costumeId == data.puchi && it.costumeType == "puchi" }?.costumeNameEN?.let { if (it == "Plain Don") "Défaut" else it } ?: "ID: ${data.puchi}"

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val itemModifier = Modifier.weight(1f)
                                ExpressiveGridItem("Kigurumi", kigurumiName, buildImageUrl("kigurumi", findFilename("kigurumi", data.kigurumi, imagesData)), onClick = { showDialogFor = "kigurumi" }, modifier = itemModifier)
                                ExpressiveGridItem("Tête", headName, buildImageUrl("head", findFilename("head", data.head, imagesData)), onClick = { showDialogFor = "head" }, modifier = itemModifier)
                                ExpressiveGridItem("Corps", bodyName, buildImageUrl("body", findFilename("body", data.body, imagesData)), onClick = { showDialogFor = "body" }, modifier = itemModifier)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val itemModifier = Modifier.weight(1f)
                                ExpressiveGridItem("Visage", faceName, buildImageUrl("face", findFilename("face", data.face, imagesData)), onClick = { showDialogFor = "face" }, modifier = itemModifier)
                                ExpressiveGridItem("Puchi", puchiName, buildImageUrl("puchi", findFilename("puchi", data.puchi, imagesData)), onClick = { showDialogFor = "puchi" }, modifier = itemModifier)
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingSection("Affichage et Recherche") {
                        SettingToggle("Montrer le panel des succès", data.isDisplayAchievement ?: false) {
                            localSettings = data.copy(isDisplayAchievement = it)
                        }

                        SettingToggle("Afficher les musques SouUchi", data.isDisplaySouUchi ?: false) {
                            localSettings = data.copy(isDisplaySouUchi = it)
                        }
                        SettingToggle("Activer la voix de Don-chan en jeu", data.isVoiceOn ?: true) {
                            localSettings = data.copy(isVoiceOn = it)
                        }

                        val achievementRankPanelUrl = getAchievementRankPanelUrl(data.achievementDisplayDifficulty)
                        if (achievementRankPanelUrl != null) {
                            AsyncImage(
                                model = achievementRankPanelUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .padding(vertical = 8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        SettingDropdown(
                            label = "Affichage de la carte de progression : Difficulté",
                            description = "Affiche la carte de progression en fonction de la difficulté choisie.",
                            selectedOption = getAchievementDisplayDifficultyName(data.achievementDisplayDifficulty),
                            options = listOf("Désactivé", "Facile", "Normal", "Difficile", "Oni", "Oni/Ura"),
                            onOptionSelected = { localSettings = data.copy(achievementDisplayDifficulty = findAchievementDisplayDifficultyId(it)) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SettingDropdown(
                            label = "Mode recherche : Difficulté",
                            description = "Propose le menu de filtre de la difficulté choisie.",
                            selectedOption = getCourseName(data.difficultySettingCourse),
                            options = listOf("Désactivé", "Configurer à chaque fois", "Normal", "Difficile", "Oni", "Ura"),
                            onOptionSelected = { localSettings = data.copy(difficultySettingCourse = findDifficultySettingCourse(it)) }
                        )
                        SettingDropdown(
                            label = "Mode recherche : Étoiles",
                            description = "Propose le menu de filtre de la l'étoile de clear choisie.",
                            selectedOption = getStarName(data.difficultySettingStar),
                            options = listOf("Désactivé", "Configurer à chaque fois", "Défaut", "Pas Clear", "Pas Full Combo", "Pas Donderful Combo"),
                            onOptionSelected = { localSettings = data.copy(difficultySettingStar = findDifficultySettingStar(it)) }
                        )
                        SettingDropdown(
                            label = "Mode recherche : Tri",
                            description = "Propose le menu de filtre de tri choisi.",
                            selectedOption = getSortName(data.difficultySettingSort),
                            options = (1..10).map { "★ $it" },
                            onOptionSelected = { localSettings = data.copy(difficultySettingSort = findDifficultySettingSort(it)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingSection("Paramètres de gameplay") {
                        data.playSetting?.let { play ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val itemModifier = Modifier.weight(1f)
                                ExpressiveGridItem("Vitesse", getSpeedName(play.speed), buildImageUrl("speed", findFilename("speed", play.speed, imagesData)), type = "speed", onClick = { showDialogFor = "speed" }, modifier = itemModifier)
                                
                                val randomImg = when(play.randomType) {
                                    1 -> "Random_Whimsical.png"
                                    2 -> "Random_Messy.png"
                                    else -> null
                                }
                                ExpressiveGridItem("Random", when(play.randomType) {
                                    1 -> "Capricieux"
                                    2 -> "Chaotique"
                                    else -> "Normal"
                                }, if (randomImg != null) buildImageUrl("random", randomImg) else null, type = "random", onClick = { showDialogFor = "random" }, modifier = itemModifier)
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            SettingToggle("Disparition", play.isVanishOn ?: false) {
                                localSettings = data.copy(playSetting = play.copy(isVanishOn = it))
                            }
                            SettingToggle("Inverse", play.isInverseOn ?: false) {
                                localSettings = data.copy(playSetting = play.copy(isInverseOn = it))
                            }

                            SettingDropdown(
                                label = "Son des tambours",
                                selectedOption = getToneName(data.toneId),
                                options = listOf(
                                    "Taiko", "Festival", "Dogs & Cats", "Taiko Deluxe", "Drumset",
                                    "Tambourine", "Wadadon", "Clapping", "Conga", "8-bit Taiko",
                                    "Heave-ho", "Mecha Don", "Funassyi", "Rap", "Hosogai",
                                    "Akemi", "Synth Drum", "Shuriken", "Bubble Pop", "Electric Guitar"
                                ),
                                onOptionSelected = { localSettings = data.copy(toneId = findTone(it)) }
                            )
                        }
                    }


                    Spacer(modifier = Modifier.height(16.dp))

                    SettingSection("Position des notes") {
                        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Décalage", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = (data.notesPosition ?: 0).let { if (it > 0) "+$it" else it.toString() },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = (data.notesPosition ?: 0).toFloat(),
                                onValueChange = {
                                    val newValue = it.roundToInt()
                                    if (newValue != data.notesPosition) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        localSettings = data.copy(notesPosition = newValue)
                                    }
                                },
                                valueRange = -5f..5f,
                                steps = 9,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp)) // Spacer for FAB
                }
            } ?: Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Text("Aucune donnée disponible", modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    // Dialogs
    showDialogFor?.let { type ->
        val items = when (type) {
            "kigurumi" -> imagesData?.images?.costumes?.kigurumi?._files ?: emptyList()
            "head" -> imagesData?.images?.costumes?.head?._files ?: emptyList()
            "body" -> imagesData?.images?.costumes?.body?._files ?: emptyList()
            "face" -> imagesData?.images?.costumes?.face?._files ?: emptyList()
            "puchi" -> imagesData?.images?.costumes?.puchi?._files ?: emptyList()
            "speed" -> (imagesData?.images?.speed?._files ?: emptyList()).sortedBy { extractId(it) }
            "title" -> imagesData?.images?.nameplates?._files ?: emptyList()
            "random" -> listOf("Normal", "Random_Whimsical.png", "Random_Messy.png")
            else -> emptyList()
        }

        val dialogTitle = when (type) {
            "kigurumi" -> "Choisir le Kigurumi"
            "head" -> "Choisir la tête"
            "body" -> "Choisir le corps"
            "face" -> "Choisir le visage"
            "puchi" -> "Choisir le Puchi"
            "speed" -> "Choisir la vitesse"
            "title" -> "Choisir la plaque de titre"
            "random" -> "Choisir le mode aléatoire"
            else -> "Choisir $type"
        }

        SelectionDialog(
            title = dialogTitle,
            items = items,
            type = type,
            costumes = costumes,
            onDismiss = { showDialogFor = null },
            onSelect = { selectedValue ->
                localSettings?.let { current ->
                    localSettings = when (type) {
                        "kigurumi" -> current.copy(kigurumi = extractId(selectedValue))
                        "head" -> current.copy(head = extractId(selectedValue))
                        "body" -> current.copy(body = extractId(selectedValue))
                        "face" -> current.copy(face = extractId(selectedValue))
                        "puchi" -> current.copy(puchi = extractId(selectedValue))
                        "speed" -> current.copy(playSetting = current.playSetting?.copy(speed = extractId(selectedValue)))
                        "title" -> current.copy(titlePlateId = extractPlateId(selectedValue))
                        "random" -> current.copy(playSetting = current.playSetting?.copy(randomType = when(selectedValue) {
                            "Random_Whimsical.png" -> 1
                            "Random_Messy.png" -> 2
                            else -> 0
                        }))
                        else -> current
                    }
                }
                showDialogFor = null
            }
        )
    }

    if (showTitleSearchDialog) {
        TitleSelectionDialog(
            titles = titles,
            onDismiss = { showTitleSearchDialog = false },
            onSelect = { selectedTitle ->
                localSettings?.let { current ->
                    localSettings = current.copy(title = selectedTitle)
                }
                showTitleSearchDialog = false
            }
        )
    }
}

@Composable
fun TitleSelectionDialog(
    titles: TaikoServerTitlesResponse?,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredTitles = remember(searchQuery, titles) {
        titles?.values?.filter {
            (it.titleName?.contains(searchQuery, ignoreCase = true) == true ||
            it.titleNameEN?.contains(searchQuery, ignoreCase = true) == true) &&
            it.titleId != 0
        }?.sortedBy { it.titleId } ?: emptyList()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Choisir un titre",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    placeholder = { Text("Rechercher un titre...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                )

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTitles) { title ->
                        Surface(
                            onClick = { onSelect(title.titleNameEN ?: title.titleName ?: "") },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ListItem(
                                headlineContent = { 
                                    Text(
                                        title.titleNameEN ?: title.titleName ?: "",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    ) 
                                },
                                supportingContent = { 
                                    Text(
                                        "ID: ${title.titleId}", 
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    ) 
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Fermer", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        }
    }
}

fun extractId(filename: String): Int {
    // filename pattern: type-0000.webp or 0.png
    return filename.substringAfterLast("-").substringBefore(".").toIntOrNull() ?: 0
}

fun extractPlateId(filename: String): Int {
    if (filename == "nameplate.webp") return -1 // Default? Or some other logic
    val suffix = filename.substringAfter("nameplate_").substringBefore(".")
    return when {
        suffix == "Wood" -> 0
        suffix == "Rainbow" -> 1
        suffix == "Gold" -> 2
        suffix == "Purple" -> 3
        suffix.startsWith("AI_") -> suffix.substringAfter("AI_").toIntOrNull()?.let { it + 3 } ?: 0
        suffix == "Onp_1" -> 8
        suffix == "Toho_Y22_QR" -> 9
        suffix.startsWith("Toho_Y22_") -> suffix.substringAfter("Toho_Y22_").toIntOrNull()?.let { it + 9 } ?: 0
        suffix.startsWith("AprilFool_") -> suffix.substringAfter("AprilFool_").toIntOrNull()?.let { it + 14 } ?: 0
        else -> 0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingDropdown(
    label: String,
    selectedOption: String,
    options: List<String>,
    description: String? = null,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        if (description != null) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                shape = RoundedCornerShape(50)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onOptionSelected(selectionOption)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
fun SelectionDialog(
    title: String,
    items: List<String>,
    type: String,
    costumes: List<TaikoServerCostume>? = null,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredItems = remember(items, searchQuery, costumes, type) {
        if (searchQuery.isBlank()) {
            items
        } else {
            items.filter { item ->
                val id = extractId(item)
                val idStr = id.toString()
                val costume = costumes?.find { it.costumeId == id && it.costumeType == type }
                val name = costume?.costumeName ?: ""
                val nameEn = costume?.costumeNameEN ?: ""
                
                idStr.contains(searchQuery, ignoreCase = true) ||
                name.contains(searchQuery, ignoreCase = true) ||
                nameEn.contains(searchQuery, ignoreCase = true) ||
                getItemDisplayName(type, item).contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (type != "speed" && type != "random") {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        placeholder = { Text("Rechercher...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Effacer")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (type == "speed") 2 else 3),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredItems) { item ->
                        Surface(
                            onClick = { onSelect(item) },
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.aspectRatio(if (type == "speed") 1.1f else 0.85f),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center, 
                                    modifier = Modifier.weight(1f).fillMaxWidth()
                                ) {
                                    if (item == "Normal") {
                                        Surface(
                                            modifier = Modifier.size(42.dp),
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    "OFF",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                    } else {
                                        val url = buildImageUrl(type, item)
                                        AsyncImage(
                                            model = url,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize().then(
                                                if (type == "puchi") Modifier.scale(2.2f).offset( y= -(10).dp, x= (12).dp) else Modifier
                                            ),
                                            contentScale = ContentScale.Fit,
                                            filterQuality = if (type == "speed" || type == "random") FilterQuality.None else FilterQuality.Low
                                        )
                                    }
                                }
                                
                                val id = extractId(item)
                                val costume = costumes?.find { it.costumeId == id && it.costumeType == type }
                                val rawName = costume?.costumeNameEN ?: costume?.costumeName
                                val name = if (rawName == "Plain Don") "Défaut" else rawName

                                Text(
                                    text = name ?: getItemDisplayName(type, item),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 2,
                                    minLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth()
                                )
                                
                                if (name != null) {
                                    Text(
                                        text = "ID: $id",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Fermer", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        }
    }
}

fun buildImageUrl(type: String, filename: String?): String? {
    if (filename == null) return null
    val baseUrl = "https://taiko.farewell.dev/images/"
    return when (type) {
        "kigurumi", "head", "body", "face", "puchi" -> "${baseUrl}Costumes/$type/$filename"
        "speed" -> "${baseUrl}Speed/$filename"
        "title" -> "${baseUrl}Nameplates/$filename"
        "random" -> "${baseUrl}$filename"
        else -> "${baseUrl}$filename"
    }
}

fun getItemDisplayName(type: String, item: String): String {
    return when (type) {
        "speed" -> "Vitesse ${getSpeedName(extractId(item))}"
        "random" -> when(item) {
            "Random_Whimsical.png" -> "Capricieux"
            "Random_Messy.png" -> "Chaotique"
            else -> "Normal"
        }
        "kigurumi", "head", "body", "face", "puchi" -> "ID: ${extractId(item)}"
        "title" -> if (item.startsWith("nameplate_")) {
            item.substringAfter("nameplate_").substringBefore(".").replace("_", " ")
        } else "Défaut"
        else -> item
    }
}

fun findDifficultySettingCourse(id: String): Int? = when (id) {
    "Désactivé" -> 0
    "Configurer à chaque fois" -> 1
    "Normal" -> 2
    "Difficile" -> 3
    "Oni" -> 4
    "Ura" -> 5
    else -> null
}

fun getCourseName(id: Int?): String = when (id) {
    0 -> "Désactivé"
    1 -> "Configurer à chaque fois"
    2 -> "Normal"
    3 -> "Difficile"
    4 -> "Oni"
    5 -> "Ura"
    else -> "Désactivé"
}

fun getAchievementDisplayDifficultyName(id: Int?): String = when (id) {
    0 -> "Désactivé"
    1 -> "Facile"
    2 -> "Normal"
    3 -> "Difficile"
    4 -> "Oni"
    5 -> "Ura"
    else -> "Désactivé"
}

fun findAchievementDisplayDifficultyId(name: String): Int? = when (name) {
    "Désactivé" -> 0
    "Facile" -> 1
    "Normal" -> 2
    "Difficile" -> 3
    "Oni" -> 4
    "Oni/Ura" -> 5
    else -> null
}

fun getAchievementRankPanelUrl(id: Int?): String? {
    val filename = when (id) {
        1 -> "rank_panel_Easy.webp"
        2 -> "rank_panel_Normal.webp"
        3 -> "rank_panel_Hard.webp"
        4 -> "rank_panel_Oni.webp"
        5 -> "rank_panel_Ura_Oni.webp"
        else -> null
    }
    return buildImageUrl("rank_panel", filename)
}

fun findTone(id: String?): Int? = when (id){
    "Taiko" -> 0
    "Festival" -> 1
    "Dogs & Cats" -> 2
    "Taiko Deluxe" -> 3
    "Drumset" -> 4
    "Tambourine" -> 5
    "Wadadon" -> 6
    "Clapping" -> 7
    "Conga" -> 8
    "8-bit Taiko" -> 9
    "Heave-ho" -> 10
    "Mecha Don" -> 11
    "Funassyi" -> 12
    "Rap" -> 13
    "Hosogai" -> 14
    "Akemi" -> 15
    "Synth Drum" -> 16
    "Shuriken" -> 17
    "Bubble Pop" -> 18
    "Electric Guitar" -> 19
    else -> null
}

fun getToneName(id: Int?): String = when (id) {
    0 -> "Taiko"
    1 -> "Festival"
    2 -> "Dogs & Cats"
    3 -> "Deluxe Taiko"
    4 -> "Drumset"
    5 -> "Tambourine"
    6 -> "Wadadon"
    7 -> "Clapping"
    8 -> "Conga"
    9 -> "8-bit Taiko"
    10 -> "Heave-ho"
    11 -> "Mecha Don"
    12 -> "Funassyi"
    13 -> "Rap"
    14 -> "Hosogai"
    15 -> "Akemi"
    16 -> "Synth Drum"
    17 -> "Shuriken"
    18 -> "Bubble Pop"
    19 -> "Electric Guitar"
    else -> "Taiko"
}

fun findDifficultySettingSort(id: String): Int? = when (id) {
    "★ 1" -> 0
    "★ 2" -> 1
    "★ 3" -> 2
    "★ 4" -> 3
    "★ 5" -> 4
    "★ 6" -> 5
    "★ 7" -> 6
    "★ 8" -> 7
    "★ 9" -> 8
    "★ 10" -> 9
    else -> null
}

fun getSortName(id: Int?): String = if (id != null && id in 0..9) "★ ${id + 1}" else "★ 1"

fun findDifficultySettingStar(id: String): Int? = when (id) {
    "Désactivé" -> 0
    "Configurer à chaque fois" -> 1
    "Défaut" -> 2
    "Pas Clear" -> 3
    "Pas Full Combo" -> 4
    "Pas Donderful Combo" -> 5
    else -> null
}

fun getStarName(id: Int?): String = when (id) {
    0 -> "Désactivé"
    1 -> "Configurer à chaque fois"
    2 -> "Défaut"
    3 -> "Pas Clear"
    4 -> "Pas Full Combo"
    5 -> "Pas Donderful Combo"
    else -> "Désactivé"
}

fun getSpeedName(id: Int?): String = when (id) {
    0 -> "x1.0"
    1 -> "x1.1"
    2 -> "x1.2"
    3 -> "x1.3"
    4 -> "x1.4"
    5 -> "x1.5"
    6 -> "x1.6"
    7 -> "x1.7"
    8 -> "x1.8"
    9 -> "x1.9"
    10 -> "x2.0"
    11 -> "x2.5"
    12 -> "x3.0"
    13 -> "x3.5"
    14 -> "x4.0"
    else -> "x1.0"
}

fun findFilename(type: String, id: Int?, imagesData: TaikoImagesData?): String? {
    if (id == null || imagesData == null) return null
    val files = when (type) {
        "kigurumi" -> imagesData.images.costumes?.kigurumi?._files
        "head" -> imagesData.images.costumes?.head?._files
        "body" -> imagesData.images.costumes?.body?._files
        "face" -> imagesData.images.costumes?.face?._files
        "puchi" -> imagesData.images.costumes?.puchi?._files
        "speed" -> imagesData.images.speed?._files
        else -> null
    }
    return files?.find { extractId(it) == id }
}

fun findPlateFilename(id: Int?, imagesData: TaikoImagesData?): String? {
    if (id == null || imagesData == null) return null
    val suffix = when (id) {
        0 -> "Wood"
        1 -> "Rainbow"
        2 -> "Gold"
        3 -> "Purple"
        in 4..7 -> "AI_${id - 3}"
        8 -> "Onp_1"
        9 -> "Toho_Y22_QR"
        in 10..14 -> "Toho_Y22_${id - 9}"
        in 15..20 -> "AprilFool_${id - 14}"
        else -> null
    }
    if (suffix == null) return "nameplate.webp"
    val target = "nameplate_$suffix.webp"
    return imagesData.images.nameplates?._files?.find { it == target } ?: "nameplate.webp"
}

@Composable
fun ProfileHeader(
    settings: TaikoServerUserSettingsResponse,
    imagesData: TaikoImagesData?,
    nameplateUrls: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Character Preview Area
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background decoration circle
                Surface(
                    modifier = Modifier.size(140.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {}

                // Character
                Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                    settings.body?.let { id ->
                        val url = buildImageUrl("body", findFilename("body", id, imagesData))
                        AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    }
                    settings.face?.let { id ->
                        val url = buildImageUrl("face", findFilename("face", id, imagesData))
                        AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    }
                    settings.head?.let { id ->
                        val url = buildImageUrl("head", findFilename("head", id, imagesData))
                        AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    }
                    settings.kigurumi?.let { id ->
                        if (id > 0) {
                            val url = buildImageUrl("kigurumi", findFilename("kigurumi", id, imagesData))
                            AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                        }
                    }

                    // Puchi integrated (animation from source)
                    settings.puchi?.let { id ->
                        val url = buildImageUrl("puchi", findFilename("puchi", id, imagesData))
                        if (url != null) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Puchi",
                                modifier = Modifier
                                    //.offset(x = -(30).dp, y = (10).dp)
                                    .size(150.dp)
                                    .padding(bottom = 4.dp, end = 4.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }

            // Nameplate Preview Area
            TaikoNameplate(
                name = settings.myDonName,
                title = settings.title,
                nameplateUrls = nameplateUrls,
                collapsedFraction = 1f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun SettingSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                content = content
            )
        }
    }
}

@Composable
fun ExpressiveGridItem(
    label: String,
    value: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: String? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(140.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = if (type == "speed" || type == "random") ContentScale.Fit else ContentScale.Fit,
                        filterQuality = if (type == "speed" || type == "random") FilterQuality.None else FilterQuality.Low
                    )
                } else if (type == "random") {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "OFF",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SettingItem(label: String, value: String, onClick: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SettingToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        onClick = { onCheckedChange(!checked) },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Switch(
                checked = checked, 
                onCheckedChange = onCheckedChange,
                thumbContent = if (checked) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else null
            )
        }
    }
}
