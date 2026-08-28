package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
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
    var localSettings by remember(serverSettings) { mutableStateOf(serverSettings) }
    var isSaving by remember { mutableStateOf(false) }

    var showDialogFor by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        taikoViewModel.fetchImagesData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
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
                                taikoViewModel.updateUserSettings(it)
                                isSaving = false
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
                    ProfileHeader(data, imagesData, titles)
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    SettingSection("Profil") {
                        SettingItem("Pseudo", data.myDonName ?: "Inconnu")
                        
                        val titleName = titles?.get(data.titlePlateId.toString())?.titleNameEN ?: data.title ?: "Inconnu"
                        ExpressiveGridItem(
                            label = "Titre",
                            value = titleName,
                            imageUrl = buildImageUrl("title", findPlateFilename(data.titlePlateId, imagesData)),
                            onClick = { showDialogFor = "title" },
                            modifier = Modifier.fillMaxWidth().height(120.dp)
                        )
                        SettingItem("BAID", data.baid?.toString() ?: "N/A")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingSection("Costume") {
                        val kigurumiName = costumes?.find { it.costumeId == data.kigurumi && it.costumeType == "kigurumi" }?.costumeNameEN ?: "ID: ${data.kigurumi}"
                        val headName = costumes?.find { it.costumeId == data.head && it.costumeType == "head" }?.costumeNameEN ?: "ID: ${data.head}"
                        val bodyName = costumes?.find { it.costumeId == data.body && it.costumeType == "body" }?.costumeNameEN ?: "ID: ${data.body}"
                        val faceName = costumes?.find { it.costumeId == data.face && it.costumeType == "face" }?.costumeNameEN ?: "ID: ${data.face}"
                        val puchiName = costumes?.find { it.costumeId == data.puchi && it.costumeType == "puchi" }?.costumeNameEN ?: "ID: ${data.puchi}"

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

                    SettingSection("Affichage") {
                        SettingToggle("Montrer le panel des succès", data.isDisplayAchievement ?: false) {
                            localSettings = data.copy(isDisplayAchievement = it)
                        }
                        SettingToggle("Montrer le Dan sur la nameplate", data.isDisplayDanOnNamePlate ?: false) {
                            localSettings = data.copy(isDisplayDanOnNamePlate = it)
                        }
                        SettingToggle("SouUchi", data.isDisplaySouUchi ?: false) {
                            localSettings = data.copy(isDisplaySouUchi = it)
                        }
                        SettingToggle("Voix", data.isVoiceOn ?: true) {
                            localSettings = data.copy(isVoiceOn = it)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingSection("Paramètres de jeu") {
                        data.playSetting?.let { play ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val itemModifier = Modifier.weight(1f)
                                ExpressiveGridItem("Vitesse", "x${(play.speed ?: 0) + 1}", buildImageUrl("speed", findFilename("speed", play.speed, imagesData)), onClick = { showDialogFor = "speed" }, modifier = itemModifier)
                                
                                val randomImg = when(play.randomType) {
                                    1 -> "Random_Whimsical.png"
                                    2 -> "Random_Messy.png"
                                    else -> null
                                }
                                ExpressiveGridItem("Random", when(play.randomType) {
                                    1 -> "Capricieux"
                                    2 -> "Chaotique"
                                    else -> "Normal"
                                }, if (randomImg != null) buildImageUrl("random", randomImg) else null, onClick = { showDialogFor = "random" }, modifier = itemModifier)
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            SettingToggle("Vanish", play.isVanishOn ?: false) {
                                localSettings = data.copy(playSetting = play.copy(isVanishOn = it))
                            }
                            SettingToggle("Inverse", play.isInverseOn ?: false) {
                                localSettings = data.copy(playSetting = play.copy(isInverseOn = it))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingSection("Personnalisation") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) { SettingItem("Face", data.faceColor?.toString() ?: "0") }
                            Box(modifier = Modifier.weight(1f)) { SettingItem("Corps", data.bodyColor?.toString() ?: "0") }
                            Box(modifier = Modifier.weight(1f)) { SettingItem("Membres", data.limbColor?.toString() ?: "0") }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Dernière partie : ${data.lastPlayDateTime ?: "Inconnue"}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )

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
            "speed" -> imagesData?.images?.speed?._files ?: emptyList()
            "title" -> imagesData?.images?.nameplates?._files ?: emptyList()
            "random" -> listOf("Normal", "Random_Whimsical.png", "Random_Messy.png")
            else -> emptyList()
        }

        SelectionDialog(
            title = "Choisir $type",
            items = items,
            type = type,
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

@Composable
fun SelectionDialog(
    title: String,
    items: List<String>,
    type: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxHeight(0.7f)
            ) {
                items(items) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clickable { onSelect(item) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            if (item == "Normal") {
                                Text("Normal")
                            } else {
                                val url = buildImageUrl(type, item)
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
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

fun findFilename(type: String, id: Int?, imagesData: org.arcade.atomcity.data.remote.model.taikoserver.TaikoImagesData?): String? {
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

fun findPlateFilename(id: Int?, imagesData: org.arcade.atomcity.data.remote.model.taikoserver.TaikoImagesData?): String? {
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
    imagesData: org.arcade.atomcity.data.remote.model.taikoserver.TaikoImagesData?,
    titles: org.arcade.atomcity.data.remote.model.taikoserver.gamedata.TaikoServerTitlesResponse?
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
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {}

                // Puchi
                settings.puchi?.let { id ->
                    val url = buildImageUrl("puchi", findFilename("puchi", id, imagesData))
                    if (url != null) {
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            modifier = Modifier.size(70.dp).align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 10.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

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
                }
            }

            // Nameplate Preview Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                val baseUrl = "https://taiko.farewell.dev/images/Nameplates/"
                
                // Nameplate layers
                AsyncImage(model = "${baseUrl}nameplate.webp", contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
                
                if (settings.isDisplayDanOnNamePlate == true) {
                    AsyncImage(model = "${baseUrl}nameplate_dan.webp", contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
                }
                
                val plateFilename = findPlateFilename(settings.titlePlateId, imagesData)
                AsyncImage(model = "${baseUrl}$plateFilename", contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)

                // Nickname & Title
                Column(
                    modifier = Modifier.fillMaxWidth().padding(start = 55.dp, end = 20.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    val titleName = titles?.get(settings.titlePlateId.toString())?.titleNameEN ?: settings.title ?: ""
                    Text(
                        text = titleName,
                        style = MaterialTheme.typography.labelSmall,
                        color = androidx.compose.ui.graphics.Color.Black,
                        maxLines = 1
                    )
                    Text(
                        text = settings.myDonName ?: "Inconnu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.Black,
                        maxLines = 1
                    )
                }
            }
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
    modifier: Modifier = Modifier
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
                        contentScale = ContentScale.Fit
                    )
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
