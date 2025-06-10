package org.arcade.atomcity.ui.guide.apistatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.utils.ApiKeyManager

@Composable
internal fun ApiCheckList() {
    val context = LocalContext.current
    val apiKeyManager = ApiKeyManager(context)

    fun getApiKey(game: String): Boolean {
        return apiKeyManager.hasApiKey(game)
    }

    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ApiItem(
            name = "maimai",
            hasKey = getApiKey("maimai")
        )
        ApiItem(
            name = "SOUND VOLTEX",
            hasKey = getApiKey("sdvx")
        )
        ApiItem(
            name = "In The Groove 2",
            hasKey = getApiKey("itg")
        )
        ApiItem(
            name = "beatmania IIDX",
            hasKey = getApiKey("iidx")
        )
        ApiItem(
            name = "pop'n music",
            hasKey = getApiKey("popn")
        )
        ApiItem(
            name = "Taiko no Tatsujin",
            hasKey = getApiKey("taiko")
        )
    }
}