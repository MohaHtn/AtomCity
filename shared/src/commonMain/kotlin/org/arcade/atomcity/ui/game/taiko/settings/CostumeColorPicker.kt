package org.arcade.atomcity.ui.game.taiko.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

val CostumeColors = listOf(
    "#F84828", "#68C0C0", "#DC1500", "#F8F0E0", "#009687", "#00BF87",
    "#00FF9A", "#66FFC2", "#FFFFFF", "#690000", "#FF0000", "#FF6666",
    "#FFB3B3", "#00BCC2", "#00F7FF", "#66FAFF", "#B3FDFF", "#E4E4E4",
    "#993800", "#FF5E00", "#FF9E78", "#FFCFB3", "#005199", "#0088FF",
    "#66B8FF", "#B3DBFF", "#B9B9B9", "#B37700", "#FFAA00", "#FFCC66",
    "#FFE2B3", "#000C80", "#0019FF", "#6675FF", "#B3BAFF", "#858585",
    "#B39B00", "#FFDD00", "#FFFF00", "#FFFF71", "#2B0080", "#5500FF",
    "#9966FF", "#CCB3FF", "#505050", "#38A100", "#78C900", "#B3FF00",
    "#DCFF8A", "#610080", "#C400FF", "#DC66FF", "#EDB3FF", "#232323",
    "#006600", "#00B800", "#00FF00", "#8AFF9E", "#990059", "#FF0095",
    "#FF66BF", "#FFB3DF", "#000000"
)

@Composable
fun ColorPickerRow(label: String, selectedIndex: Int, onColorSelected: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(CostumeColors.size) { index ->
                    val color = Color(parseColor(CostumeColors[index]))
                    Surface(
                        onClick = { 
                            onColorSelected(index)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = color,
                        border = if (selectedIndex == index) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        if (selectedIndex == index) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isColorDark(color)) Color.White else Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun parseColor(hex: String): Long {
    return hex.removePrefix("#").toLong(16) or 0xFF000000L
}

fun isColorDark(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance < 0.5
}
