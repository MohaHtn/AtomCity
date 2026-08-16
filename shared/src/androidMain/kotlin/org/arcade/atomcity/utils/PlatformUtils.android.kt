package org.arcade.atomcity.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

actual object PlatformUtils {
    actual val isIos: Boolean = false
    actual val isAndroid: Boolean = true
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

    actual fun shareImage(bitmap: ImageBitmap, context: Any?) {
        val androidContext = context as? android.content.Context ?: return
        val androidBitmap = bitmap.asAndroidBitmap()

        try {
            val cachePath = File(androidContext.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "maimai_b30.webp")
            val fileOutputStream = FileOutputStream(file)
            
            val compressFormat = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }
            
            androidBitmap.compress(compressFormat, 80, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            val uri = FileProvider.getUriForFile(
                androidContext,
                "org.arcade.atomcity.provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/webp"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            androidContext.startActivity(Intent.createChooser(intent, "Partager mon B30"))
        } catch (e: Exception) {
            log("PlatformUtils", "Error sharing image: ${e.message}", true)
        }
    }
}

@Composable
actual fun rememberPlatformContext(): Any? = LocalContext.current
