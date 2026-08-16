package org.arcade.atomcity.utils

import androidx.compose.runtime.Composable
import platform.Foundation.NSLog
import platform.Foundation.NSString
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import kotlinx.cinterop.*
import platform.CoreCrypto.*
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UISelectionFeedbackGenerator
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaImage
import org.jetbrains.skia.EncodedImageFormat
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ByteVar

actual object PlatformUtils {
    actual val isIos: Boolean = true
    actual val isAndroid: Boolean = false
    actual fun log(tag: String, message: String, isError: Boolean) {
        NSLog("[%s] %s", tag, message)
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
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

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual fun shareImage(bitmap: ImageBitmap, context: Any?) {
        val skiaImage = bitmap.asSkiaImage()
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
}

@Composable
actual fun rememberPlatformContext(): Any? = null
