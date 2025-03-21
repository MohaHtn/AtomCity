package org.arcade.atomcity.ui.core


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.util.ApiKeyManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.arcade.atomcity.ui.navigation.Screen

const val welcomeTitle = "Bienvenue !"
const val welcomeTextAppIntro = "Cette application vous permet de consulter vos score et ceux de vos amis sur vos jeux de rythmes préférés à Atom City ! (et eventuellement plus encore mais on verra hein)"
const val welcomeTextAtomIntro = "Si vous ne savez pas, Atom City est une salle d'arcade située à Lille, qui propose de nombreux jeux d'arcades directement importés du japon, et non seulement des jeux de rythmes !"
const val setupTextIntro = "Pour commencer, il vous faudra créer et indiquer votre clé API pour chaque serveur de jeu que vous souhaitez consulter."
const val setupTextAPI = "Pour l'instant, voici les jeux disponibles et ceux que vous avez déjà configuré :"


var page1 = mutableStateOf(true)
var openApiGuide = mutableStateOf(false)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(navController: NavController) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = welcomeTitle)
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
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
                    SetupCard(navController)
                }
            }
        }
    }
}

@Composable
fun WelcomeCard() {
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
            onClick = { toPage2() },
        ) {
            Text(
                text = "Continuer",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun SetupCard(navController: NavController) {
    val sharedPreferences: SharedPreferences = LocalContext.current.getSharedPreferences("api_prefs", Context.MODE_PRIVATE)
    val apiKeyManager = ApiKeyManager(sharedPreferences)


    //TODO : refaire cette merde pour pas c/c le code nul là
    fun hasApiKey(game: String): Boolean {
        return apiKeyManager.hasApiKey(game)
    }


    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
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

        Button(
            onClick = { toPage1() },
        ) { Text(
            text = "Retour",
            style = MaterialTheme.typography.labelMedium
        )}

        // TODO : maimai est forcé ici pour le moment
        if(hasApiKey("maimai")) {
            Button(
                onClick = { navController.navigate(Screen.Game.createRoute("maimai")) },
                ) { Text(
                text = "Suivant",
                style = MaterialTheme.typography.labelMedium
            )}
        }


    }

}

@Composable
private fun ApiCheckList() {

    val sharedPreferences: SharedPreferences = LocalContext.current.getSharedPreferences("api_prefs", Context.MODE_PRIVATE)
    val apiKeyManager = ApiKeyManager(sharedPreferences)

    fun hasApiKey(game: String): Boolean {
        return apiKeyManager.hasApiKey(game)
    }

    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ApiItem(
            name = "maimai",
            hasKey = hasApiKey("maimai")
        )
        ApiItem(
            name = "SOUND VOLTEX",
            hasKey = hasApiKey("sdvx")
        )
        ApiItem(
            name = "In The Groove 2",
            hasKey = hasApiKey("itg")
        )
        ApiItem(
            name = "beatmania IIDX",
            hasKey = hasApiKey("iidx")
        )
        ApiItem(
            name = "pop'n music",
            hasKey = hasApiKey("popn")
        )
        ApiItem(
            name = "Taiko no Tatsujin",
            hasKey = hasApiKey("taiko")
        )

    }
}

@Composable
private fun ApiItem(
    name: String,
    hasKey: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (hasKey) {
                    Icons.Rounded.CheckCircle
                } else {
                    Icons.Rounded.Close
                },
                contentDescription = if (hasKey) "API configurée" else "API non configurée",
                tint = if (hasKey) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            if (!hasKey) {
                Button(
                    modifier = Modifier.padding(start = 56.dp),
                    onClick = { /* TODO */ }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Annuler"
                        )
                        Text(
                            text = "Ajouter",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            else
                Button(
                modifier = Modifier.padding(start = 56.dp),
                    onClick = { openApiGuide.value = true ; Log.d("API", "Clicked ${openApiGuide.value}")}
                ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        imageVector = Icons.Filled.Create,
                        contentDescription = "Modifier"
                    )
                    Text(
                        text = "Modifier",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

fun toPage2(){
    page1.value = false
}

fun toPage1(){
    page1.value = true
}

