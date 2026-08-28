package org.arcade.atomcity.ui.core

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val annotatedString = buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { index, line ->
            val normalizedLine = line.removePrefix("\uFEFF").trimStart()
            when {
                normalizedLine.startsWith("# ") -> {
                    withStyle(style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                        color = linkColor
                    )) {
                        append(normalizedLine.removePrefix("# "))
                    }
                }
                normalizedLine.startsWith("### ") -> {
                    withStyle(style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        color = linkColor
                    )) {
                        append(normalizedLine.removePrefix("### "))
                    }
                }
                normalizedLine.startsWith("## ") -> {
                    withStyle(style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        color = linkColor
                    )) {
                        append(normalizedLine.removePrefix("## "))
                    }
                }
                normalizedLine.startsWith("- ") -> {
                    append("  • ")
                    parseInlineMarkdown(normalizedLine.removePrefix("- "), linkColor)
                }
                else -> {
                    parseInlineMarkdown(normalizedLine, linkColor)
                }
            }
            if (index < lines.size - 1) {
                append("\n")
            }
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.parseInlineMarkdown(
    text: String,
    linkColor: androidx.compose.ui.graphics.Color
) {
    val boldPattern = "\\*\\*(.*?)\\*\\*".toRegex()
    val italicPattern = "\\*(.*?)\\*".toRegex()
    val markdownLinkPattern = "\\[([^\\]]+)\\]\\(([^)]+)\\)".toRegex()
    val bareUrlPattern = "https?://[^\\s)]+".toRegex()

    var lastIndex = 0

    val matches = (
        boldPattern.findAll(text).map { it to "bold" } +
            italicPattern.findAll(text).map { it to "italic" } +
            markdownLinkPattern.findAll(text).map { it to "link" } +
            bareUrlPattern.findAll(text).map { it to "url" }
    )
        .sortedWith(compareBy<Pair<MatchResult, String>> { it.first.range.first }
            .thenByDescending { it.first.range.last - it.first.range.first })

    matches.forEach { (match, type) ->
        if (match.range.first < lastIndex) {
            return@forEach
        }

        append(text.substring(lastIndex, match.range.first))

        when (type) {
            "bold" -> withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(match.groupValues[1])
            }
            "italic" -> withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                append(match.groupValues[1])
            }
            "link" -> {
                val label = match.groupValues[1]
                val url = match.groupValues[2]
                val linkStart = length
                append(label)
                val linkEnd = length
                addLink(
                   url = LinkAnnotation.Url(
                       url = url,
                       styles = TextLinkStyles(
                           style = SpanStyle(
                               color = linkColor,
                               textDecoration = TextDecoration.Underline
                           )
                       )
                   ),
                   start = linkStart,
                   end = linkEnd
                )
            }
            "url" -> {
                val url = match.value
                val linkStart = length
                append(url)
                val linkEnd = length
                addLink(
                   url = LinkAnnotation.Url(
                       url = url,
                       styles = TextLinkStyles(
                           style = SpanStyle(
                               color = linkColor,
                               textDecoration = TextDecoration.Underline
                           )
                       )
                   ),
                   start = linkStart,
                   end = linkEnd
                )
            }
        }

        lastIndex = match.range.last + 1
    }

    append(text.substring(lastIndex))
}
