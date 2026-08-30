package org.arcade.atomcity.utils

import coil3.ComponentRegistry
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder

actual fun ComponentRegistry.Builder.addAnimatedDecoders(): ComponentRegistry.Builder {
    if (android.os.Build.VERSION.SDK_INT >= 28) {
        add(AnimatedImageDecoder.Factory())
    }
    add(GifDecoder.Factory())
    return this
}
