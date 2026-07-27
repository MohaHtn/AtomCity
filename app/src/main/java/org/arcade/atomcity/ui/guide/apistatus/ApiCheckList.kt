package org.arcade.atomcity.ui.guide.apistatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.arcade.atomcity.ui.theme.AtomCityTheme

@Composable
internal fun ApiCheckList() {
    Column(        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ApiItem(
            name = "maimai",
            key = "maimai",
        )
        ApiItem(
            name = "SOUND VOLTEX",
            key = "sdvx",
        )
        ApiItem(
            name = "In The Groove 2",
            key = "itg",
        )
        ApiItem(
            name = "beatmania IIDX",
            key = "iidx",
        )
        ApiItem(
            name = "pop'n music",
            key = "popn",
        )
        ApiItem(
            name = "Taiko no Tatsujin",
            key = "taiko",
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ApiCheckListPreview() {
    AtomCityTheme {
        ApiCheckList()
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun ApiCheckListPreviewNew() {
    AtomCityTheme {
        ApiCheckList()
    }
}

