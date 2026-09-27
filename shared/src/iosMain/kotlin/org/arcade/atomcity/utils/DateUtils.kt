package org.arcade.atomcity.utils

import platform.Foundation.*

/**
 * Formate une date ISO en format lisible.
 * @param playDate la date au format ISO à formater (peut être null)
 * @param isUtc true si la date en entrée est en UTC, false si elle est en heure locale
 * @param offsetHours décalage horaire en heures à appliquer
 * @return une chaîne formatée "jour mois année, heure:minute" ou chaîne vide si null
 */
actual fun formatPlayDate(playDate: String?, isUtc: Boolean, offsetHours: Long): String {
    return playDate?.let { rawString ->
        if (rawString.isBlank()) return ""
        val normalized = rawString.trim().replace(" ", "T")
        try {
            val isoFormatter = NSDateFormatter().apply {
                dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
                timeZone = if (isUtc) (NSTimeZone.timeZoneWithName("UTC") ?: NSTimeZone.timeZoneForSecondsFromGMT(0)) else NSTimeZone.localTimeZone
            }
            
            val cleanIso = if (normalized.length >= 19) normalized.substring(0, 19) else normalized
            var date = isoFormatter.dateFromString(cleanIso)
            if (date != null && offsetHours != 0L) {
                date = date.dateByAddingTimeInterval(offsetHours.toDouble() * 3600.0)
            }
            
            if (date != null) {
                val outputFormatter = NSDateFormatter().apply {
                    dateFormat = "d MMMM yyyy, HH:mm"
                    locale = NSLocale.currentLocale
                    timeZone = NSTimeZone.localTimeZone
                }
                outputFormatter.stringFromDate(date)
            } else {
                rawString
            }
        } catch (e: Exception) {
            rawString
        }
    } ?: ""
}

actual fun getCurrentFormattedDate(): String {
    val formatter = NSDateFormatter().apply {
        dateFormat = "dd/MM/yyyy HH:mm"
        locale = NSLocale.currentLocale
        timeZone = NSTimeZone.localTimeZone
    }
    return formatter.stringFromDate(NSDate())
}
