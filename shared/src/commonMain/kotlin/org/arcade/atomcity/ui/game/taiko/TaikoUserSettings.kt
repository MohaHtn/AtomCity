package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import org.arcade.atomcity.data.remote.model.taikoserver.TaikoImagesData
import org.arcade.atomcity.data.remote.model.taikoserver.gamedata.TaikoServerCostume
import org.arcade.atomcity.data.remote.model.taikoserver.gamedata.TaikoServerTitlesResponse
import org.arcade.atomcity.data.remote.model.taikoserver.usersettings.TaikoServerUserSettingsResponse
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel
import org.arcade.atomcity.ui.core.InfoCard
import org.arcade.atomcity.ui.game.taiko.settings.*

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
                title = {
                    taikoViewModel.userSettingsData.value?.myDonName?.let {
                        Column {
                            Text(
                                text = "Profil du joueur",
                                maxLines = 1,
                                fontWeight = FontWeight.Bold,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = it,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                },
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
                    SettingSection("Prévisualisation de Don-chan"){
                        ProfileHeader(
                            data,
                            imagesData,
                            nameplateUrls,
                            taikoViewModel,
                            titleFontSize = 14.sp,
                            nameFontSize = 14.sp)

                        InfoCard("Les couleurs sont utilisables uniquement si vous avez sélectionné aucun kigurumi. Les couleurs des membres sont uniquement visible en jeu.", modifier = Modifier.padding(bottom = 8.dp))

                        ColorPickerRow("Visage", data.faceColor ?: 0) { localSettings = data.copy(faceColor = it) }
                        ColorPickerRow("Corps", data.bodyColor ?: 0) { localSettings = data.copy(bodyColor = it) }
                        ColorPickerRow("Membres (Non visible sur la prévisualisation)", data.limbColor ?: 0) { localSettings = data.copy(limbColor = it) }

                        SettingToggle("Montrer le Dan sur la nameplate", data.isDisplayDanOnNamePlate ?: false) {
                            localSettings = data.copy(isDisplayDanOnNamePlate = it)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    SettingSection("Profil de jeu") {
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



                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingSection("Costume") {
                        val kigurumiName = costumes?.find { it.costumeId == data.kigurumi && it.costumeType == "kigurumi" }?.costumeNameEN?.let { if (it == "Plain Don") "Défaut" else it } ?: "ID: ${data.kigurumi}"
                        val headName = costumes?.find { it.costumeId == data.head && it.costumeType == "head" }?.costumeNameEN?.let { if (it == "Plain Don") "Défaut" else it } ?: "ID: ${data.head}"
                        val bodyName = costumes?.find { it.costumeId == data.body && it.costumeType == "body" }?.costumeNameEN?.let { if (it == "Plain Don") "Défaut" else it } ?: "ID: ${data.body}"
                        val faceName = costumes?.find { it.costumeId == data.face && it.costumeType == "face" }?.costumeNameEN?.let { if (it == "Plain Don") "Défaut" else it } ?: "ID: ${data.face}"
                        val puchiName = costumes?.find { it.costumeId == data.puchi && it.costumeType == "puchi" }?.costumeNameEN?.let { if (it == "Plain Don") "Défaut" else it } ?: "ID: ${data.puchi}"

                        val isKigurumiSelected = (data.kigurumi ?: 0) > 0

                        if (isKigurumiSelected) {
                            InfoCard(
                                message = "Si vous mettez un Kigurumi, la tête et le corps seront désactivés. De même, vous risquez de ne pas voir les couleurs choisies plus haut.")
                            Spacer(modifier = Modifier.height(8.dp))
                        }

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
                                ExpressiveGridItem(
                                    label = "Tête",
                                    value = if (isKigurumiSelected) "N/A" else headName,
                                    imageUrl = if (isKigurumiSelected) null else buildImageUrl("head", findFilename("head", data.head, imagesData)),
                                    onClick = { if (!isKigurumiSelected) showDialogFor = "head" },
                                    modifier = itemModifier.then(if (isKigurumiSelected) Modifier.alpha(0.5f) else Modifier)
                                )
                                ExpressiveGridItem(
                                    label = "Corps",
                                    value = if (isKigurumiSelected) "N/A" else bodyName,
                                    imageUrl = if (isKigurumiSelected) null else buildImageUrl("body", findFilename("body", data.body, imagesData)),
                                    onClick = { if (!isKigurumiSelected) showDialogFor = "body" },
                                    modifier = itemModifier.then(if (isKigurumiSelected) Modifier.alpha(0.5f) else Modifier)
                                )
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
            taikoViewModel = taikoViewModel,
            settings = localSettings,
            onSelect = { selectedValue ->
                localSettings?.let { current ->
                    localSettings = when (type) {
                        "kigurumi" -> {
                            val id = extractId(selectedValue)
                            if (id > 0) {
                                current.copy(kigurumi = id, head = 0, body = 0)
                            } else {
                                current.copy(kigurumi = id)
                            }
                        }
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
