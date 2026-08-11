package org.arcade.atomcity.utils

import kotlinx.datetime.*
import kotlin.time.ExperimentalTime

/**
 * Formate une date ISO en format lisible.
 * @param playDate la date au format ISO à formater (peut être null)
 * @return une chaîne formatée "jour mois année, heure:minute" ou chaîne vide si null
 */
@OptIn(ExperimentalTime::class)
fun formatPlayDate(playDate: String?): String {
    return playDate?.let { dateString ->
        try {
            // "2023-10-27T10:00:00Z" -> we take the first 19 chars
            val instant = Instant.parse(dateString.substring(0, 19) + "Z")
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            
            // Simple formatting as commonMain doesn't have a rich formatter by default
            val day = localDateTime.dayOfMonth
            val month = localDateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }
            val year = localDateTime.year
            val hour = localDateTime.hour.toString().padStart(2, '0')
            val minute = localDateTime.minute.toString().padStart(2, '0')
            
            "$day $month $year, $hour:$minute"
        } catch (e: Exception) {
            dateString
        }
    } ?: ""
}
