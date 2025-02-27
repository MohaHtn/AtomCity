package org.arcade.atomcity.ui.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AchievementChip(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    shape: Shape = MaterialTheme.shapes.small,
    elevation: Dp = 4.dp
) {
    AssistChip(
        onClick = { /* Do something */ },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = backgroundColor,
            labelColor = contentColor
        ),
        shape = shape,
        elevation = AssistChipDefaults.assistChipElevation(elevation),
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    )
}

@Preview(showBackground = false)
@Composable
fun AchievementChipPreview() {
    Box(
        modifier = Modifier
            .padding(16.dp)
    ) {
        AchievementChip(
            text = "98.12",
        )
    }
}