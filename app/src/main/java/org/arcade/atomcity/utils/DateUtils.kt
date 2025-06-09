package org.arcade.atomcity.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.os.Build

/**
 * Formate une date ISO en format lisible.
 * @param playDate la date au format ISO à formater (peut être null)
 * @return une chaîne formatée "jour mois année, heure:minute" ou chaîne vide si null
 */
fun formatPlayDate(playDate: String?): String {
    return playDate?.let { dateString ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val dateTime = LocalDateTime.parse(
                dateString.substring(0, 19),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
            )
            val date = dateTime.format(
                DateTimeFormatter.ofPattern("d MMMM yyyy")
            )
            val time = dateTime.format(
                DateTimeFormatter.ofPattern("HH:mm")
            )
            "$date, $time"
        } else {
            try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                val outputDateFormat = java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale.getDefault())
                val outputTimeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                val date = inputFormat.parse(dateString.substring(0, 19))
                "${outputDateFormat.format(date)}, ${outputTimeFormat.format(date)}"
            } catch (e: Exception) {
                dateString // Retourne la chaîne brute en cas d'erreur
            }
        }
    } ?: ""
}