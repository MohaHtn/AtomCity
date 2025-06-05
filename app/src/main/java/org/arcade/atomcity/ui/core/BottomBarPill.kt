package org.arcade.atomcity.ui.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun BottomBarPill(
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    onHomeClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth())
    {
        Box(
            modifier = Modifier
                .height(40.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if (currentPage > 1) {
                            onPageChange(currentPage - 1)
                        }
                    },
                    modifier = Modifier.height(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous page"
                    )
                }
                Button(
                    onClick = onHomeClick,
                    modifier = Modifier.height(50.dp)
                ) {
                    Text(
                        text = "Jeux"
                    )
                }
                Button(
                    onClick = onSettingsClick,
                    modifier = Modifier.height(50.dp)
                ) {
                    Text(
                        text = "Paramètres"
                    )
                }

                Button(
                    onClick = { onPageChange(currentPage + 1) },
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next page"
                    )
                }
            }
        }
    }
}
