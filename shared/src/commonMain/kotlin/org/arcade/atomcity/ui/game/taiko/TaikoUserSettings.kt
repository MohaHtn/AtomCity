package org.arcade.atomcity.ui.game.taiko

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.presentation.viewmodel.TaikoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaikoUserSettings(
    taikoViewModel: TaikoViewModel,
    onBackClick: () -> Unit
) {
    val settings by taikoViewModel.userDetailedSettings.collectAsState()
    val costumes by taikoViewModel.costumesData.collectAsState()
    val titles by taikoViewModel.titlesData.collectAsState()
    val isLoading by taikoViewModel.isLoadingUserSettings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres Taiko") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }
        } else {
            settings?.let { data ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SettingSection("Profil") {
                        SettingItem("Pseudo", data.myDonName ?: "Inconnu")
                        
                        val titleName = titles?.get(data.titlePlateId.toString())?.titleNameEN ?: data.title ?: "Inconnu"
                        SettingItem("Titre", titleName)
                        SettingItem("BAID", data.baid?.toString() ?: "N/A")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingSection("Costume") {
                        val kigurumiName = costumes?.find { it.costumeId == data.kigurumi && it.costumeType == "kigurumi" }?.costumeNameEN ?: "Défaut"
                        val headName = costumes?.find { it.costumeId == data.head && it.costumeType == "head" }?.costumeNameEN ?: "Défaut"
                        val bodyName = costumes?.find { it.costumeId == data.body && it.costumeType == "body" }?.costumeNameEN ?: "Défaut"
                        val faceName = costumes?.find { it.costumeId == data.face && it.costumeType == "face" }?.costumeNameEN ?: "Défaut"
                        val puchiName = costumes?.find { it.costumeId == data.puchi && it.costumeType == "puchi" }?.costumeNameEN ?: "Défaut"

                        SettingItem("Kigurumi", kigurumiName)
                        SettingItem("Tête", headName)
                        SettingItem("Corps", bodyName)
                        SettingItem("Visage", faceName)
                        SettingItem("Puchi", puchiName)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingSection("Affichage") {
                        SettingToggle("Succès", data.isDisplayAchievement ?: false)
                        SettingToggle("Dan sur plaque", data.isDisplayDanOnNamePlate ?: false)
                        SettingToggle("SouUchi", data.isDisplaySouUchi ?: false)
                        SettingToggle("Voix", data.isVoiceOn ?: true)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingSection("Paramètres de jeu") {
                        data.playSetting?.let { play ->
                            SettingItem("Vitesse", "x${(play.speed ?: 0) + 1}")
                            SettingToggle("Vanish", play.isVanishOn ?: false)
                            SettingToggle("Inverse", play.isInverseOn ?: false)
                            SettingItem("Random", when(play.randomType) {
                                1 -> "Capricieux"
                                2 -> "Chaotique"
                                else -> "Normal"
                            })
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingSection("Personnalisation") {
                        SettingItem("Couleur Face", data.faceColor?.toString() ?: "0")
                        SettingItem("Couleur Corps", data.bodyColor?.toString() ?: "0")
                        SettingItem("Couleur Membres", data.limbColor?.toString() ?: "0")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Dernière partie : ${data.lastPlayDateTime ?: "Inconnue"}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } ?: Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Text("Aucune donnée disponible", modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }
        }
    }
}

@Composable
fun SettingSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        content()
    }
}

@Composable
fun SettingItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SettingToggle(label: String, checked: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = null, enabled = false)
    }
}
