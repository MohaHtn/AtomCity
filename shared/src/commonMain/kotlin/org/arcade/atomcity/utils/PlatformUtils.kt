package org.arcade.atomcity.utils

import androidx.compose.runtime.Composable

expect object PlatformUtils {
    val isIos: Boolean
    val isAndroid: Boolean
    fun log(tag: String, message: String, isError: Boolean = false)
    fun sha256(text: String): String
    fun currentTimeMillis(): Long
    fun hapticTick()
    fun hapticImpact()
    fun shareImage(bitmap: androidx.compose.ui.graphics.ImageBitmap, context: Any? = null)
    fun encrypt(text: String): String
    fun decrypt(encryptedText: String): String
    fun exitApp()
}

@Composable
expect fun rememberPlatformContext(): Any?
