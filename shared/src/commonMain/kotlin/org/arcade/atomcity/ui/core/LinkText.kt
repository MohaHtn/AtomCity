package org.arcade.atomcity.ui.core

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun LinkText(
    fullText: String,
    linkText: String = "",
    url: String = "",
    links: List<Pair<String, String>> = emptyList(),
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface,
    )
) {
    val allLinks = if (links.isNotEmpty()) links else listOf(linkText to url)

    val annotatedString = buildAnnotatedString {
        append(fullText)

        for ((lText, lUrl) in allLinks) {
            if (lText.isNotBlank()) {
                val startIndex = fullText.indexOf(lText)
                if (startIndex >= 0) {
                    val endIndex = startIndex + lText.length
                    addLink(
                        url = LinkAnnotation.Url(
                            url = lUrl,
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                        ),
                        start = startIndex,
                        end = endIndex
                    )
                }
            }
        }
    }

    Text(
        text = annotatedString,
        style = style,
        modifier = modifier.padding(bottom = 8.dp)
    )
}
