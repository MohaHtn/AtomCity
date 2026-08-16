package org.arcade.atomcity.utils

import platform.Foundation.*

/**
 * Formate une date ISO en format lisible.
 * @param playDate la date au format ISO à formater (peut être null)
 * @return une chaîne formatée "jour mois année, heure:minute" ou chaîne vide si null
 */
actual fun formatPlayDate(playDate: String?): String {
    return playDate?.let { dateString ->
        try {
            val isoFormatter = NSDateFormatter().apply {
                dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
                timeZone = NSTimeZone.timeZoneWithName("UTC") ?: NSTimeZone.timeZoneForSecondsFromGMT(0)
            }
            
            val date = isoFormatter.dateFromString(dateString.substring(0, 19))
            
            if (date != null) {
                val outputFormatter = NSDateFormatter().apply {
                    // d MMMM yyyy, HH:mm
                    // MMMM will be localized based on the current locale of the device
                    dateFormat = "d MMMM yyyy, HH:mm"
                    locale = NSLocale.currentLocale
                    timeZone = NSTimeZone.localTimeZone
                }
                outputFormatter.stringFromDate(date)
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
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
