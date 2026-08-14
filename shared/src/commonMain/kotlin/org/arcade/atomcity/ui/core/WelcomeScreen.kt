package org.arcade.atomcity.ui.core

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.ui.guide.apistatus.ApiCheckList
import org.arcade.atomcity.utils.ApiKeyManager
import org.koin.compose.koinInject

const val welcomeTitle = "Bienvenue !"
const val welcomeTextAppIntro = "Cette application vous permet de consulter vos score et ceux de vos amis sur vos jeux de rythmes préférés à Atom City ! (et eventuellement plus encore mais on verra hein)"
const val welcomeTextAtomIntro = "Si vous ne savez pas, Atom City est une salle d'arcade située à Lille, qui propose de nombreux jeux d'arcades directement importés du japon, et non seulement des jeux de rythmes !"
const val setupTextIntro = "Pour commencer, il vous faudra créer et indiquer votre clé API pour chaque serveur de jeu que vous souhaitez consulter."
const val setupTextAPI = "Pour l'instant, voici les jeux disponibles et ceux que vous avez déjà configuré :"

const val setupWarningScreen =
    "Votre clé API sera communiqué et stocké de manière sécurisée sur un serveur distant, " +
    "afin de pouvoir récupérer de manière periodique vos scores. De plus, l'ajout de votre clé API sur cette " +
    "application vous permettra d'être visible aux yeux d'autres utilisateurs, en montrant vos scores dans un leaderboard. \n\n" +
    "À tout moment vous avez le choix de supprimer votre clé du serveur distant en allant dans les paramètres de " +
    "l'application, ou de révoquer votre clé API directement sur le portail de MaiTea. Dans ce cas, le serveur distant supprimera la clé également.\n\n" +
    "En ajoutant votre clé API, vous reconnaissiez que vous êtes au courant de cette fonctionnalité. Si vous n'êtes pas d'accord, n'utilisez pas l'application."

var page1 = mutableStateOf(true)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onContinueClick: (String) -> Unit
) {
    val apiKeyManager: ApiKeyManager = koinInject()
    val apiChecklist by apiKeyManager.getApiChecklistStateFlow().collectAsState(initial = emptyList())
    val scrollState = rememberScrollState()

    LaunchedEffect(apiChecklist) {
        GlobalUIState.availableApiKeys.value = apiChecklist
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = welcomeTitle)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = page1.value,
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(durationMillis = 200)
                    ) togetherWith fadeOut(
                        animationSpec = tween(durationMillis = 200)
                    )
                }

            ) { isPage1 ->
                if (isPage1) {
                    WelcomeCard()
                } else {
                    SetupCard(apiChecklist, onContinueClick)
                }
            }
        }
    }
}

@Composable
fun WelcomeCard() {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        WarningDialog(
            onDismissRequest = { showDialog = false },
            onConfirm = {
                showDialog = false
                page1.value = false
            }
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column {
                Text(
                    modifier = Modifier.padding(24.dp),
                    text = "Bienvenue à Atom City !",
                    style = MaterialTheme.typography.headlineSmall
                )
                HorizontalDivider()
                Text(
                    modifier = Modifier.padding(24.dp),
                    text = welcomeTextAppIntro,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    modifier = Modifier.padding(24.dp, 0.dp, 24.dp, 24.dp),
                    text = welcomeTextAtomIntro,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Button(
            onClick = { showDialog = true },
        ) {
            Text(
                text = "Continuer",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun WarningDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    var checked by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "\uD83D\uDEA8 Information importante concernant maimai",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = setupWarningScreen,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { checked = !checked }
                        .padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { checked = it }
                    )
                    Text(
                        text = "J'ai pris en compte l'information ci-dessus et je suis d'accord.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = checked
            ) {
                Text("Accepter et continuer")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun SetupCard(
    apiChecklist: List<String>,
    onContinueClick: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column {
                Text(
                    modifier = Modifier.padding(24.dp),
                    text = setupTextIntro,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    modifier = Modifier.padding(24.dp, 0.dp, 24.dp, 24.dp),
                    text = setupTextAPI,
                    style = MaterialTheme.typography.bodyLarge
                )

                HorizontalDivider()
                
                ApiCheckList()
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(24.dp)
        ) {
            Button(
                onClick = { page1.value = true },
            ) {
                Text(
                    text = "Retour",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            if (apiChecklist.isNotEmpty()) {
                Button(
                    onClick = { onContinueClick(apiChecklist.first()) }
                )
                {
                    Text(
                        text = "Suivant",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
