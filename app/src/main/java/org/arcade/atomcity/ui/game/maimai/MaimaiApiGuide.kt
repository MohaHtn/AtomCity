package org.arcade.atomcity.ui.game.maimai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf

val maimaiApiGuide = "Guide d'ajout de la clé API pour maimai"

@Composable
fun MaimaiApiGuide(visible: MutableState<Boolean>, onDismiss: () -> Unit) {

    val isVisible: MutableState<Boolean> = remember { mutableStateOf(false) }

    LaunchedEffect(visible.value) {
        isVisible.value = visible.value
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = {onDismiss()}),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isVisible.value,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            // Fond semi-transparent
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(onClick = {onDismiss()})
            )
        }
        AnimatedVisibility(
            visible = isVisible.value,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(500)
            ) + fadeIn(animationSpec = tween(500)),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(500)
            ) + fadeOut(animationSpec = tween(500))
        ) {
            Card(
                modifier = Modifier
                    .zIndex(1f)
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Guide d'ajout de la clé API pour maimai",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = maimaiApiGuide,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}