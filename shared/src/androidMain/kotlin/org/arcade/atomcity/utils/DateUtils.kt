package org.arcade.atomcity.utils

import android.os.Build
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val cleanIso = if (normalized.length >= 19) normalized.substring(0, 19) else normalized
                val parsedDateTime = LocalDateTime.parse(
                    cleanIso,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                )

                var zonedDateTime = if (isUtc) {
                    parsedDateTime.atZone(ZoneId.of("UTC"))
                        .withZoneSameInstant(ZoneId.systemDefault())
                } else {
                    parsedDateTime.atZone(ZoneId.systemDefault())
                }

                if (offsetHours != 0L) {
                    zonedDateTime = zonedDateTime.plusHours(offsetHours)
                }

                val date = zonedDateTime.format(
                    DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())
                )
                val time = zonedDateTime.format(
                    DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
                )
                "$date, $time"
            } catch (e: Exception) {
                rawString
            }
        } else {
            try {
                val cleanIso = if (normalized.length >= 19) normalized.substring(0, 19) else normalized
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                if (isUtc) {
                    inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                }

                val parsedDate = inputFormat.parse(cleanIso)
                val dateMs = parsedDate!!.time + (offsetHours * 3600000L)
                val date = Date(dateMs)

                val outputDateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
                val outputTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                
                "${outputDateFormat.format(date)}, ${outputTimeFormat.format(date)}"
            } catch (e: Exception) {
                rawString
            }
        }
    } ?: ""
}

actual fun getCurrentFormattedDate(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm "))
    } else {
        SimpleDateFormat("dd/MM/yyyy 'à' HH:mm", Locale.getDefault()).format(Date())
    }
}
