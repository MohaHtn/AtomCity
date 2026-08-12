package org.arcade.atomcity.utils

import android.os.Build
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

/**
 * Formate une date ISO en format lisible.
 * @param playDate la date au format ISO à formater (peut être null)
 * @return une chaîne formatée "jour mois année, heure:minute" ou chaîne vide si null
 */
actual fun formatPlayDate(playDate: String?): String {
    return playDate?.let { dateString ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                // On suppose que la date reçue est en UTC
                val utcDateTime = LocalDateTime.parse(
                    dateString.substring(0, 19),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                )
                val zonedDateTime = utcDateTime.atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(ZoneId.systemDefault())
                    .minusHours(1)

                val date = zonedDateTime.format(
                    DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())
                )
                val time = zonedDateTime.format(
                    DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
                )
                "$date, $time"
            } catch (e: Exception) {
                dateString
            }
        } else {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")

                val parsedDate = inputFormat.parse(dateString.substring(0, 19))
                // On enlève une heure parce que jsp (3600000 ms)
                val date = java.util.Date(parsedDate!!.time - 3600000)

                val outputDateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
                val outputTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                
                "${outputDateFormat.format(date)}, ${outputTimeFormat.format(date)}"
            } catch (e: Exception) {
                dateString
            }
        }
    } ?: ""
}
