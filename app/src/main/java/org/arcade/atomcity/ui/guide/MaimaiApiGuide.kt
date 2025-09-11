package org.arcade.atomcity.ui.guide

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.arcade.atomcity.R
import org.arcade.atomcity.presentation.viewmodel.MaiteaViewModel
import org.arcade.atomcity.ui.core.LinkText
import org.arcade.atomcity.ui.core.openApiGuide
import org.arcade.atomcity.utils.ApiKeyManager

const val MAIMAI_API_GUIDE_TITLE = "Ajouter une clé API maitea pour maimai"
const val MAIMAI_API_GUIDE_TEXT = "Pour obtenir votre clé API, rendez-vous sur maitea.app et connectez-vous. Si vous n'avez pas de compte, n'hésitez pas à vous en créer un ! Une fois connecté, allez sur 'Profile' dans le menu déroulant."
const val MAIMAI_API_GUIDE_TEXT2 = "Vous arriverez dans cette page."
const val MAIMAI_API_GUIDE_TEXT3 = "En bas de la page, une section 'Access Token' est disponible. Cliquez sur le bouton 'Create Access Token'."
const val MAIMAI_API_GUIDE_URL = "https://maitea.app"
const val MAIMAI_API_GUIDE_STEP1 = "Etape 1 Accès à MaiTea"
const val MAIMAI_API_GUIDE_STEP2 = "Etape 2 Bouton de création de la clé API"
const val MAIMAI_API_GUIDE_STEP3 = "Étape 3 création de la clé API"
const val MAIMAI_API_GUIDE_STEP4 = "Étape 4 Message de confirmation de création de la clé API"
const val MAIMAI_API_GUIDE_SUCCESS = "Lorsque vous voyez le message de succès, vous pouvez copier la clé API et la coller juste en bas."
const val MAIMAI_API_GUIDE_LABEL = "Clé API"
const val MAIMAI_API_GUIDE_PLACEHOLDER = "Exemple: 391|UBvwFPZvDrC3lm9DMSd50e4zXZicB5ssPogJmsw"
const val MAIMAI_API_GUIDE_VALIDATE = "Valider"
const val MAIMAI_API_GUIDE_BACK = "Retour"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaimaiApiGuide(apiKeyManager: ApiKeyManager, isVisible: MutableState<Boolean>, maiteaViewModel: MaiteaViewModel) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = {
            openApiGuide.value = false
        },
        sheetState = sheetState
    ) {
        LazyColumn (
            modifier = Modifier.padding(16.dp)
        ) {
            item {
                MaimaiApiGuideContent(apiKeyManager = apiKeyManager, isVisible = isVisible, maiteaViewModel = maiteaViewModel)
            }
        }
    }
}

@Composable
fun MaimaiApiGuideContent(apiKeyManager: ApiKeyManager, isVisible: MutableState<Boolean>, maiteaViewModel: MaiteaViewModel) {
    val showSnackbar = remember { mutableStateOf(false) }

    Text(
        text = MAIMAI_API_GUIDE_TITLE,
        style = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center),
        modifier = Modifier.padding(16.dp),
    )

    LinkText(
        fullText = MAIMAI_API_GUIDE_TEXT,
        linkText = "maitea.app",
        url = MAIMAI_API_GUIDE_URL,
    )

    Image(
        painter = painterResource(id = R.drawable.guide_maimai_step1),
        contentDescription = MAIMAI_API_GUIDE_STEP1,
        modifier = Modifier
            .fillMaxWidth()
            .width(300.dp)
            .height(300.dp)
            .padding(vertical = 8.dp),
        contentScale = ContentScale.Fit
    )

    Text(
        text = MAIMAI_API_GUIDE_TEXT2,
        style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Justify),
        modifier = Modifier.padding(16.dp)
    )

    Image(
        painter = painterResource(id = R.drawable.guide_maimai_step2),
        contentDescription = MAIMAI_API_GUIDE_STEP2,
        modifier = Modifier
            .fillMaxWidth()
            .width(300.dp)
            .height(300.dp)
            .padding(vertical = 8.dp),
        contentScale = ContentScale.Fit
    )

    Text(
        modifier = Modifier.padding(16.dp),
        text = MAIMAI_API_GUIDE_TEXT3,
        style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Justify)
    )

    Image(
        painter = painterResource(id = R.drawable.guide_maimai_step3),
        contentDescription = MAIMAI_API_GUIDE_STEP3,
        modifier = Modifier
            .fillMaxWidth()
            .width(300.dp)
            .height(300.dp)
            .padding(vertical = 8.dp),
    )

    Text(
        modifier = Modifier.padding(16.dp),
        text = MAIMAI_API_GUIDE_SUCCESS,
        style = MaterialTheme.typography.bodyMedium
    )

    Image(
        painter = painterResource(id = R.drawable.guide_maimai_step4),
        contentDescription = MAIMAI_API_GUIDE_STEP4,
        modifier = Modifier
            .fillMaxWidth()
            .width(300.dp)
            .height(300.dp)
            .padding(vertical = 8.dp),
        contentScale = ContentScale.Fit
    )

    EnterApiTextBox(apiKeyManager = apiKeyManager, isVisible = isVisible, showSnackbar = showSnackbar, maiteaViewModel = maiteaViewModel)

    if (showSnackbar.value) {
        SnackbarMessage("Clé API enregistrée avec succès")
    }
}

@Composable
fun EnterApiTextBox(apiKeyManager: ApiKeyManager, isVisible: MutableState<Boolean>, showSnackbar: MutableState<Boolean>, maiteaViewModel: MaiteaViewModel) {
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val isValidInput by remember(text, isError) {
        mutableStateOf(text.isNotBlank() && !isError && text.contains("|"))
    }

    val minApiKeyLength = 10

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { newText ->
                text = newText
                when {
                    newText.isBlank() -> {
                        isError = true
                        errorMessage = "La clé API ne peut pas être vide."
                    }
                    !newText.contains("|") -> {
                        isError = true
                        errorMessage = "Format invalide. La clé doit contenir le caractère '|'"
                    }
                    newText.length < minApiKeyLength -> {
                        isError = true
                        errorMessage = "La clé API est trop courte (minimum $minApiKeyLength caractères)."
                    }
                    else -> {
                        isError = false
                        errorMessage = ""
                    }
                }
            },
            label = { Text(MAIMAI_API_GUIDE_LABEL) },
            placeholder = { Text(MAIMAI_API_GUIDE_PLACEHOLDER) },
            modifier = Modifier.fillMaxWidth(),
            isError = isError,
            supportingText = {
                if (isError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            singleLine = true
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(
                onClick = { isVisible.value = false }
            ) {
                Text(MAIMAI_API_GUIDE_BACK)
            }


            Button(
                onClick = {
                    if (isValidInput) {
                        scope.launch {
                            apiKeyManager.saveApiKey("maimai", text)
                            maiteaViewModel.addApiKey(text)
                            isVisible.value = false
                        }
                    }
                },
                enabled = isValidInput,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(MAIMAI_API_GUIDE_VALIDATE)
            }
        }
    }
}

@Composable
fun SnackbarMessage(message: String) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(message) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "OK",
                duration = SnackbarDuration.Short
            )
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(16.dp)
    ) { snackbarData ->
        Snackbar(
            action = {
                TextButton(onClick = { snackbarData.dismiss() }) {
                    Text(snackbarData.visuals.actionLabel ?: "OK")
                }
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Text(snackbarData.visuals.message)
        }
    }
}