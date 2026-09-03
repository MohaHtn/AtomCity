package org.arcade.atomcity.utils

import io.ktor.util.decodeBase64String
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object TokenUtils {
    fun extractBaid(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = parts[1]

            val base64 = payload
                .replace('-', '+')
                .replace('_', '/')

            val paddedBase64 = when (base64.length % 4) {
                2 -> "$base64=="
                3 -> "$base64="
                else -> base64
            }

            val decodedString = paddedBase64.decodeBase64String()
            val jsonElement = Json.parseToJsonElement(decodedString)
            val jsonObject = jsonElement.jsonObject

            val baid = jsonObject["http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name"]?.jsonPrimitive?.content

            PlatformUtils.log("TokenUtils", "Decoded BAID: $baid")
            baid

        } catch (e: Exception) {
            PlatformUtils.log("TokenUtils", "Error extracting BAID from token: ${e.message}")
            null
        }
    }
}