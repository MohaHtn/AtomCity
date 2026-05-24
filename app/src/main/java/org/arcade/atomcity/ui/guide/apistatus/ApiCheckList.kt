package org.arcade.atomcity.ui.guide.apistatus

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.ui.theme.AtomCityTheme
import org.arcade.atomcity.utils.ApiKeyManager

@Composable
internal fun ApiCheckList(apiChecklistState: MutableState<List<String>>) {
    val context = LocalContext.current
    val apiKeyManager = ApiKeyManager(context)
    
    fun getApiChecklistState(name: String): Boolean {
        Log.d("ApiCheckList", "Checking API key for: $name, in checklist: ${apiChecklistState.value}")
        return apiChecklistState.value.contains(name.lowercase().replace(" ", ""))
    }

    Column(        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ApiItem(
            name = "maimai",
            key = "maimai",
            hasKey = getApiChecklistState("maimai")
        )
        ApiItem(
            name = "SOUND VOLTEX",
            key = "sdvx",
            hasKey = getApiChecklistState("sdvx")
        )
        ApiItem(
            name = "In The Groove 2",
            key = "itg",
            hasKey = getApiChecklistState("itg")
        )
        ApiItem(
            name = "beatmania IIDX",
            key = "iidx",
            hasKey = getApiChecklistState("iidx")
        )
        ApiItem(
            name = "pop'n music",
            key = "popn",
            hasKey = getApiChecklistState("popn")
        )
        ApiItem(
            name = "Taiko no Tatsujin",
            key = "taiko",
            hasKey = getApiChecklistState("taiko")
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ApiCheckListPreview() {
    val mockState = remember { mutableStateOf(listOf("maimai", "taiko")) }
    AtomCityTheme {
        ApiCheckList(apiChecklistState = mockState)
    }
}

