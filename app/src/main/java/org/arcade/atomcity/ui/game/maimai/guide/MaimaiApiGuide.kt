package org.arcade.atomcity.ui.game.maimai.guide

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.arcade.atomcity.R
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
fun MaimaiApiGuide(apiKeyManager: ApiKeyManager, isVisible: MutableState<Boolean>) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

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
                MaimaiApiGuideContent(apiKeyManager = apiKeyManager, isVisible = isVisible)
            }
        }
    }
}


 @Composable
 fun MaimaiApiGuideContent(apiKeyManager: ApiKeyManager, isVisible: MutableState<Boolean>) {
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

     EnterApiTextBox(apiKeyManager = apiKeyManager, isVisible = isVisible, showSnackbar = showSnackbar)

    if (showSnackbar.value) {
        SnackbarMesage("Clé API enregistrée avec succès")
    }

 }

@Composable
fun EnterApiTextBox(apiKeyManager: ApiKeyManager, isVisible: MutableState<Boolean>, showSnackbar: MutableState<Boolean>) {
    var text: MutableState<String> = remember { mutableStateOf("") }
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()


    Column {
        OutlinedTextField(
            value = text.value,
            onValueChange = { newText: String -> text.value = newText },
            label = { androidx.compose.material3.Text(MAIMAI_API_GUIDE_LABEL) },
            placeholder = { androidx.compose.material3.Text(MAIMAI_API_GUIDE_PLACEHOLDER) },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (!showSnackbar.value) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { isVisible.value = false },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(MAIMAI_API_GUIDE_BACK)
                    }

                    val scope = rememberCoroutineScope()  // Moved outside the onClick lambda

                    Button(
                        onClick = {
                            scope.launch {
                                apiKeyManager.saveApiKey("maimai", text.value)
                                Log.d("MaimaiApiGuide", "API key saved: ${text.value}")
                                showSnackbar.value = true
                                isVisible.value = false
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(MAIMAI_API_GUIDE_VALIDATE)
                    }
                }
            } else {
            }
        }
    }
}
@Composable
fun SnackbarMesage(message: String) {
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

@Composable
fun LinkText(
    fullText: String,
    linkText: String,
    url: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface
    )
) {
    val uriHandler = LocalUriHandler.current
    val textMeasurer = rememberTextMeasurer()
    val annotatedString = buildAnnotatedString {
        append(fullText)

        val startIndex = fullText.indexOf(linkText)
        val endIndex = startIndex + linkText.length

        if (startIndex >= 0) {
            addStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                ),
                start = startIndex,
                end = endIndex
            )

            addStringAnnotation(
                tag = "URL",
                annotation = url,
                start = startIndex,
                end = endIndex
            )
        }
    }

    BasicText(
        text = annotatedString,
        style = style,
        modifier = Modifier.padding(16.dp).pointerInput(Unit) {
            detectTapGestures { offset ->
                val position = annotatedString
                    .getStringAnnotations("URL", 0, annotatedString.length)
                    .firstOrNull()
                    ?.let { annotation ->
                        val textLayoutResult = textMeasurer.measure(
                            text = annotatedString,
                            style = style
                        )
                        val bounds = textLayoutResult.getBoundingBox(annotation.start)
                        bounds.contains(offset)
                    } ?: false

                if (position) {
                    uriHandler.openUri(url)
                }
            }
        }
    )
}
