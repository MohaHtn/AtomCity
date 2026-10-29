package org.arcade.atomcity.utils

import android.graphics.Bitmap
import android.os.Build
import coil3.Image
import coil3.toBitmap

actual fun Image.lightPixelRatio(): Float? {
    val source = toBitmap()
    // Hardware bitmaps can't be read pixel by pixel, so read from a software copy.
    val bitmap = if (Build.VERSION.SDK_INT >= 26 && source.config == Bitmap.Config.HARDWARE) {
        source.copy(Bitmap.Config.ARGB_8888, false) ?: return null
    } else {
        source
    }
    return sampleLightPixelRatio(bitmap.width, bitmap.height) { x, y -> bitmap.getPixel(x, y) }
}
