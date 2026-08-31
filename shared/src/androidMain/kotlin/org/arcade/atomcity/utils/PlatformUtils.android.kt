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
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

actual object PlatformUtils {
    private const val KEY_ALIAS = "atomcity_secure_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        
        val key = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (key != null) return key

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenerator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return keyGenerator.generateKey()
    }

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

    actual fun encrypt(text: String): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            val encryption = cipher.doFinal(text.toByteArray())
            
            // Format: IV|Encryption (Base64 encoded)
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val encryptionBase64 = Base64.encodeToString(encryption, Base64.NO_WRAP)
            "$ivBase64|$encryptionBase64"
        } catch (e: Exception) {
            log("PlatformUtils", "Encryption error: ${e.message}", true)
            text // Fallback to plain text if encryption fails
        }
    }

    actual fun decrypt(encryptedText: String): String {
        if (!encryptedText.contains("|")) return encryptedText
        return try {
            val parts = encryptedText.split("|")
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encryption = Base64.decode(parts[1], Base64.NO_WRAP)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
            
            String(cipher.doFinal(encryption))
        } catch (e: Exception) {
            log("PlatformUtils", "Decryption error: ${e.message}", true)
            encryptedText
        }
    }
}

@Composable
actual fun rememberPlatformContext(): Any? = LocalContext.current
