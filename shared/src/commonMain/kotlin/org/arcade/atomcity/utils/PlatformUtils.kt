package org.arcade.atomcity.utils

expect object PlatformUtils {
    val isIos: Boolean
    val isAndroid: Boolean
    fun log(tag: String, message: String, isError: Boolean = false)
    fun sha256(text: String): String
    fun currentTimeMillis(): Long
    fun hapticTick()
    fun hapticImpact()
}
