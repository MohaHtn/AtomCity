package org.arcade.atomcity.utils

import platform.Foundation.NSLog
import platform.Foundation.NSString
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import kotlinx.cinterop.*
import platform.CoreCrypto.*

actual object PlatformUtils {
    actual fun log(tag: String, message: String, isError: Boolean) {
        NSLog("[%s] %s", tag, message)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun sha256(text: String): String {
        val data = (text as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return ""
        val hash = ByteArray(CC_SHA256_DIGEST_LENGTH)
        
        hash.usePinned { pinned ->
            CC_SHA256(data.bytes, data.length.toUInt(), pinned.addressOf(0).reinterpret())
        }
        
        return hash.joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
    }

    actual fun currentTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }
}
