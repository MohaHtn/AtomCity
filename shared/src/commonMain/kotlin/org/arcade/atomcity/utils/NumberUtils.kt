package org.arcade.atomcity.utils

import kotlin.math.roundToLong

fun Double.format(digits: Int): String {
    val factor = 10.0.pow(digits)
    return ((this * factor).roundToLong() / factor).toString()
}

private fun Double.pow(n: Int): Double {
    var result = 1.0
    repeat(n) { result *= this }
    return result
}
