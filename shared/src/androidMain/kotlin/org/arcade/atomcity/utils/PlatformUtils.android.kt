package org.arcade.atomcity.utils

import android.util.Log
import java.security.MessageDigest

actual object PlatformUtils {
    actual fun log(tag: String, message: String, isError: Boolean) {
        if (isError) {
            Log.e(tag, message)
        } else {
            Log.d(tag, message)
        }
    }

    actual fun sha256(text: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(text.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    actual fun currentTimeMillis(): Long = System.currentTimeMillis()

    actual fun hapticTick() {
        // Handled via LocalHapticFeedback in Compose for Android
    }

    actual fun hapticImpact() {
        // Handled via LocalHapticFeedback in Compose for Android
    }
}
