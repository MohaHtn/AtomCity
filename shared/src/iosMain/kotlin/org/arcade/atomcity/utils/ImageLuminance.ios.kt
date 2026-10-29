package org.arcade.atomcity.utils

import coil3.Image
import coil3.toBitmap

actual fun Image.lightPixelRatio(): Float? {
    val bitmap = toBitmap()
    return sampleLightPixelRatio(bitmap.width, bitmap.height) { x, y -> bitmap.getColor(x, y) }
}
