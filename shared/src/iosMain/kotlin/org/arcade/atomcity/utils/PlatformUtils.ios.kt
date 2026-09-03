package org.arcade.atomcity.utils

import androidx.compose.runtime.Composable
import platform.Foundation.*
import kotlinx.cinterop.*
import platform.CoreCrypto.*
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UISelectionFeedbackGenerator
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.Image
import org.jetbrains.skia.EncodedImageFormat
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.Security.*
import platform.CoreFoundation.*
import platform.posix.size_tVar
import platform.posix.memcpy

@OptIn(BetaInteropApi::class)
actual object PlatformUtils {
    private const val KEY_TAG = "org.arcade.atomcity.secure_key"

    actual val isIos: Boolean = true
    actual val isAndroid: Boolean = false
    actual fun log(tag: String, message: String, isError: Boolean) {
        NSLog("[%s] %s", tag, message)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun sha256(text: String): String {
        val nsString = NSString.create(string = text)
        val data = nsString.dataUsingEncoding(NSUTF8StringEncoding) ?: return ""
        val hash = ByteArray(CC_SHA256_DIGEST_LENGTH)
        
        hash.usePinned { pinned ->
            CC_SHA256(data.bytes, data.length.toUInt(), pinned.addressOf(0).reinterpret())
        }
        
        return hash.joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
    }

    actual fun currentTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }

    actual fun hapticTick() {
        UISelectionFeedbackGenerator().selectionChanged()
    }

    actual fun hapticImpact() {
        UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium).impactOccurred()
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun shareImage(bitmap: ImageBitmap, context: Any?) {
        val skiaBitmap = bitmap.asSkiaBitmap()
        val skiaImage = Image.makeFromBitmap(skiaBitmap)
        val encodedData = skiaImage.encodeToData(EncodedImageFormat.WEBP, 80) ?: return
        val bytes = encodedData.bytes
        
        val nsData = bytes.usePinned<ByteArray, NSData> { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0).reinterpret<ByteVar>(),
                length = bytes.size.toULong()
            )
        }
        
        val uiImage = UIImage.imageWithData(nsData) ?: return
        val activityController = UIActivityViewController(listOf(uiImage), null)

        val window = UIApplication.sharedApplication.keyWindow
        var rootViewController = window?.rootViewController
        while (rootViewController?.presentedViewController != null) {
            rootViewController = rootViewController.presentedViewController
        }

        rootViewController?.presentViewController(activityController, true, null)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun encrypt(text: String): String {
        return try {
            val key = getOrGenerateKey()
            val iv = generateRandomBytes(16)
            val data = text.encodeToByteArray()
            
            val encrypted = crypt(kCCEncrypt, key, iv, data)
            val ivBase64 = iv.toNSData().base64EncodedStringWithOptions(0UL)
            val encryptedBase64 = encrypted.toNSData().base64EncodedStringWithOptions(0UL)
            "$ivBase64|$encryptedBase64"
        } catch (e: Exception) {
            log("PlatformUtils", "Encryption error: ${e.message}", true)
            text
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun decrypt(encryptedText: String): String {
        if (!encryptedText.contains("|")) return encryptedText
        return try {
            val parts = encryptedText.split("|")
            val ivData = NSData.create(base64EncodedString = parts[0], options = 0UL) ?: return encryptedText
            val encryptedData = NSData.create(base64EncodedString = parts[1], options = 0UL) ?: return encryptedText
            
            val key = getOrGenerateKey()
            val decrypted = crypt(kCCDecrypt, key, ivData.toByteArray(), encryptedData.toByteArray())
            decrypted.decodeToString()
        } catch (e: Exception) {
            log("PlatformUtils", "Decryption error: ${e.message}", true)
            encryptedText
        }
    }

    actual fun exitApp() {
        platform.posix.exit(0)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun getOrGenerateKey(): ByteArray {
        val query = CFDictionaryCreateMutable(null, 0, null, null)
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrAccount, CFBridgingRetain(NSString.create(string = KEY_TAG)))
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)

        val result = memScoped {
            val ptr = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, ptr.ptr)
            if (status == errSecSuccess) {
                val data = CFBridgingRelease(ptr.value) as NSData
                data.toByteArray()
            } else null
        }

        if (result != null) return result

        // Generate new key
        val newKey = generateRandomBytes(32)
        val addQuery = CFDictionaryCreateMutable(null, 0, null, null)
        CFDictionaryAddValue(addQuery, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(addQuery, kSecAttrAccount, CFBridgingRetain(NSString.create(string = KEY_TAG)))
        CFDictionaryAddValue(addQuery, kSecValueData, CFBridgingRetain(newKey.toNSData()))
        
        SecItemAdd(addQuery, null)
        return newKey
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun generateRandomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        bytes.usePinned { pinned ->
            SecRandomCopyBytes(kSecRandomDefault, size.toULong(), pinned.addressOf(0))
        }
        return bytes
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun crypt(op: CCOperation, key: ByteArray, iv: ByteArray, data: ByteArray): ByteArray {
        val outSize = data.size + kCCBlockSizeAES128.toInt()
        val out = ByteArray(outSize)
        
        return memScoped {
            val moved = alloc<size_tVar>()
            val status = key.usePinned { pKey ->
                iv.usePinned { pIv ->
                    data.usePinned { pIn ->
                        out.usePinned { pOut ->
                            CCCrypt(
                                op, kCCAlgorithmAES, kCCOptionPKCS7Padding,
                                pKey.addressOf(0), kCCKeySizeAES256.toULong(),
                                pIv.addressOf(0),
                                pIn.addressOf(0), data.size.toULong(),
                                pOut.addressOf(0), outSize.toULong(),
                                moved.ptr
                            )
                        }
                    }
                }
            }
            if (status != kCCSuccess) throw Exception("CCCrypt failed with status $status")
            out.copyOf(moved.value.toInt())
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun NSData.toByteArray(): ByteArray {
        val size = length.toInt()
        val bytes = ByteArray(size)
        if (size > 0) {
            bytes.usePinned { pinned ->
                memcpy(pinned.addressOf(0), this.bytes, length)
            }
        }
        return bytes
    }
}

@Composable
actual fun rememberPlatformContext(): Any? = null
