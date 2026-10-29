/**
 * Image Luminance Utilities
 *
 * Measures how light or dark a loaded Coil image is, to pick a readable text color over it.
 */
package org.arcade.atomcity.utils

import coil3.Image
import kotlin.math.max
import kotlin.math.pow

/**
 * Relative luminance above which black text contrasts better than white text (WCAG).
 */
internal const val LIGHT_PIXEL_LUMINANCE_THRESHOLD = 0.179f

/**
 * Fraction of the image's pixels, from 0 to 1, on which black text contrasts better
 * than white text, or null if its pixels can't be read. Above 0.5, the image is mostly light.
 *
 * A per-pixel vote is used instead of an average luminance: averaging linear luminance
 * lets a few bright details (white lines, highlights) tip a mostly dark image into "light".
 */
expect fun Image.lightPixelRatio(): Float?

/**
 * Samples a grid of at most ~64 pixels per side, ignoring mostly transparent pixels.
 */
internal inline fun sampleLightPixelRatio(width: Int, height: Int, pixelAt: (x: Int, y: Int) -> Int): Float? {
    if (width <= 0 || height <= 0) return null
    val step = max(1, max(width, height) / 64)

    var lightCount = 0
    var count = 0
    for (y in 0 until height step step) {
        for (x in 0 until width step step) {
            val argb = pixelAt(x, y)
            if ((argb ushr 24) and 0xFF < 128) continue
            if (relativeLuminance(argb) > LIGHT_PIXEL_LUMINANCE_THRESHOLD) lightCount++
            count++
        }
    }
    return if (count == 0) null else lightCount.toFloat() / count
}

internal fun relativeLuminance(argb: Int): Float {
    fun linearize(channel: Int): Float {
        val c = channel / 255f
        return if (c <= 0.03928f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)
    }
    val r = linearize((argb shr 16) and 0xFF)
    val g = linearize((argb shr 8) and 0xFF)
    val b = linearize(argb and 0xFF)
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}
