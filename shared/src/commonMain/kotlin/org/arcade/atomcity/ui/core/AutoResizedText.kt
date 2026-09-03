package org.arcade.atomcity.ui.core

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * A Text composable that automatically scales down its font size if the text overflows 
 * the available container width or height.
 */
@Composable
fun AutoResizedText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    minFontSize: TextUnit = 9.sp,
    maxLines: Int = 1
) {
    var resizedTextStyle by remember(text, style) { mutableStateOf(style) }
    var shouldDraw by remember(text, style) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (shouldDraw) {
                drawContent()
            }
        },
        color = color,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = TextOverflow.Clip,
        softWrap = false,
        maxLines = maxLines,
        style = resizedTextStyle,
        onTextLayout = { result ->
            if (result.didOverflowWidth || result.didOverflowHeight) {
                if (resizedTextStyle.fontSize > minFontSize) {
                    val currentSize = resizedTextStyle.fontSize.value
                    resizedTextStyle = resizedTextStyle.copy(
                        fontSize = (currentSize * 0.9f).sp
                    )
                } else {
                    shouldDraw = true
                }
            } else {
                shouldDraw = true
            }
        }
    )
}
