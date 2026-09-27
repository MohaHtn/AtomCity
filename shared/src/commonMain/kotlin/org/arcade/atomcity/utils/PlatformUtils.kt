/**
 * Platform Utilities Expect Declaration
 *
 * Declares multiplatform utility functions for platform detection, logging, hashing, haptics, encryption, and file sharing.
 */
package org.arcade.atomcity.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

expect object PlatformUtils {
    val isIos: Boolean
    val isAndroid: Boolean
    fun log(tag: String, message: String, isError: Boolean = false)
    fun sha256(text: String): String
    fun currentTimeMillis(): Long
    fun hapticTick()
    fun hapticImpact()
    fun shareImage(bitmap: ImageBitmap, context: Any? = null)
    fun saveImage(bitmap: ImageBitmap, context: Any? = null)
    fun encrypt(text: String): String
    fun decrypt(encryptedText: String): String
    fun exitApp()
}

@Composable
expect fun rememberPlatformContext(): Any?
