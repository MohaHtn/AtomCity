package org.arcade.atomcity.ui.core

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

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