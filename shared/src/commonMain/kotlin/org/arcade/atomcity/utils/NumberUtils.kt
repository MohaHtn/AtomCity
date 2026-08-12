package org.arcade.atomcity.utils

import kotlin.math.roundToLong

fun Double.format(digits: Int): String {
    val factor = 10.0.pow(digits)
    val rounded = (this * factor).roundToLong()
    val string = rounded.toString()
    
    return if (digits == 0) {
        string
    } else {
        if (string.length <= digits) {
            val padded = string.padStart(digits, '0')
            "0.$padded"
        } else {
            val integerPart = string.substring(0, string.length - digits)
            val decimalPart = string.substring(string.length - digits)
            "$integerPart.$decimalPart"
        }
    }
}

private fun Double.pow(n: Int): Double {
    var result = 1.0
    repeat(n) { result *= this }
    return result
}
