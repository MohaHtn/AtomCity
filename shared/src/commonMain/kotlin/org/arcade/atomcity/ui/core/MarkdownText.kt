package org.arcade.atomcity.ui.core

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    val annotatedString = buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { index, line ->
            when {
                line.startsWith("### ") -> {
                    withStyle(style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        color = MaterialTheme.colorScheme.primary
                    )) {
                        append(line.removePrefix("### "))
                    }
                }
                line.startsWith("## ") -> {
                    withStyle(style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        color = MaterialTheme.colorScheme.primary
                    )) {
                        append(line.removePrefix("## "))
                    }
                }
                line.startsWith("# ") -> {
                    withStyle(style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                        color = MaterialTheme.colorScheme.primary
                    )) {
                        append(line.removePrefix("# "))
                    }
                }
                line.startsWith("- ") -> {
                    append("  • ")
                    parseInlineMarkdown(line.removePrefix("- "))
                }
                else -> {
                    parseInlineMarkdown(line)
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

private fun androidx.compose.ui.text.AnnotatedString.Builder.parseInlineMarkdown(text: String) {
    val boldPattern = "\\*\\*(.*?)\\*\\*".toRegex()
    val italicPattern = "\\*(.*?)\\*".toRegex()
    
    var lastIndex = 0
    
    // This is still a very basic parser, but handles bold and italic separately
    // Note: It won't handle nested bold/italic correctly.
    
    val matches = (boldPattern.findAll(text).map { it to "bold" } + 
                   italicPattern.findAll(text).map { it to "italic" })
                  .sortedBy { it.first.range.first }

    matches.forEach { (match, type) ->
        if (match.range.first >= lastIndex) {
            append(text.substring(lastIndex, match.range.first))
            when (type) {
                "bold" -> withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groupValues[1])
                }
                "italic" -> withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(match.groupValues[1])
                }
            }
            lastIndex = match.range.last + 1
        }
    }
    append(text.substring(lastIndex))
}
