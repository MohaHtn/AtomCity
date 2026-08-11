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
    linkText: String,
    url: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface,
    )
) {
    val annotatedString = buildAnnotatedString {
        append(fullText)

        val startIndex = fullText.indexOf(linkText)
        if (startIndex >= 0) {
            val endIndex = startIndex + linkText.length
            addLink(
                url = LinkAnnotation.Url(
                    url = url,
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

    Text(
        text = annotatedString,
        style = style,
        modifier = modifier.padding(16.dp)
    )
}
