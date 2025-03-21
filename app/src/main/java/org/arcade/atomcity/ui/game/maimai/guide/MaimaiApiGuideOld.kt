package org.arcade.atomcity.ui.game.maimai.guide

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import org.arcade.atomcity.R
import androidx.navigation.compose.rememberNavController
import org.arcade.atomcity.util.ApiKeyManager
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign



@Composable
fun MaimaiApiGuideOld(visible: MutableState<Boolean>, onDismiss: () -> Unit, apiKeyManager: ApiKeyManager) {

    val isVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
    val offsetY = remember { mutableStateOf(0f) }
    val dismissThreshold = 100f
    val coroutineScope = rememberCoroutineScope()

    // Define cardExpansion and baseHeight
    val cardExpansion = remember { mutableStateOf(0f) }
    val baseHeight = 600.dp
    val maxExpansion = 300.dp

    val animatedHeight by animateDpAsState(
        targetValue = baseHeight + cardExpansion.value.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )

    val shape: Shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    LaunchedEffect(Unit) {
        isVisible.value = visible.value
    }

    LaunchedEffect(isVisible.value) {
        if (!isVisible.value) {
            delay(300)
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (offsetY.value > dismissThreshold) {
                            isVisible.value = false
                        } else {
                            coroutineScope.launch {
                                animate(
                                    initialValue = offsetY.value,
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ) { value, _ ->
                                    offsetY.value = value
                                }
                                animate(
                                    initialValue = offsetY.value,
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ) { value, _ -> offsetY.value = value
                                }
                            }
                        }
                    },
                    onDragCancel = { offsetY.value = 0f },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // Simplified calculation
                        if (dragAmount.y > 0) {
                            offsetY.value += dragAmount.y * 0.2f
                        } else if (dragAmount.y < 0) {
                            cardExpansion.value = (cardExpansion.value - dragAmount.y * 0.2f)
                                .coerceIn(0f, maxExpansion.value)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isVisible.value,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
        }
        AnimatedVisibility(
            modifier = Modifier.offset(y = 150.dp),
            visible = isVisible.value,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        ) {
            Card(
                shape = shape,
                modifier = Modifier
                    .zIndex(1f)
                    .graphicsLayer {
                        translationY = offsetY.value
                    }
                    .fillMaxWidth()
                    .heightIn(max = baseHeight + cardExpansion.value.dp)
            )
            {
            Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .width(100.dp)
                                .height(4.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragEnd = {
                                            if (offsetY.value > dismissThreshold) {
                                                isVisible.value = false
                                            } else {
                                                coroutineScope.launch {
                                                    animate(
                                                        initialValue = offsetY.value,
                                                        targetValue = 0f,
                                                        animationSpec = spring(
                                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                                            stiffness = Spring.StiffnessLow
                                                        )
                                                    ) { value, _ ->
                                                        offsetY.value = value
                                                    }
                                                }
                                            }
                                        },
                                        onDragCancel = { offsetY.value = 0f },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            offsetY.value += dragAmount.y * 0.2f
                                        }
                                    )
                                }
                                .background(Color.Gray, shape = RoundedCornerShape(8.dp))
                        )

                        Text(
                            text = MAIMAI_API_GUIDE_TITLE,
                            style = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center),
                            modifier = Modifier.pointerInput(Unit) {
                                detectDragGestures(
                                    onDragEnd = {
                                        if (offsetY.value > dismissThreshold) {
                                            isVisible.value = false
                                        } else {
                                            coroutineScope.launch {
                                                animate(
                                                    initialValue = offsetY.value,
                                                    targetValue = 0f,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessLow
                                                    )
                                                ) { value, _ ->
                                                    offsetY.value = value
                                                }
                                            }
                                        }
                                    },
                                    onDragCancel = { offsetY.value = 0f },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        offsetY.value += dragAmount.y * 0.2f
                                    }
                                )
                            }.padding(16.dp),
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


                        EnterApiTextBox(apiKeyManager, isVisible)
                    }
                }
            }
        }
    }
}


