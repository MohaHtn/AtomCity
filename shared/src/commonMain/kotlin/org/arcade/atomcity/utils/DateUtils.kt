/**
 * Date Formatting Utilities
 *
 * Provides multiplatform functions for parsing, formatting, and handling play timestamps and dates.
 */
package org.arcade.atomcity.utils

/**
 * Formate une date ISO en format lisible.
 * @param playDate la date au format ISO à formater (peut être null)
 * @param isUtc true si la date en entrée est en UTC (ex: Maimai), false si elle est en heure locale (ex: Taiko)
 * @param offsetHours décalage horaire manuel en heures à appliquer
 * @return une chaîne formatée "jour mois année, heure:minute" ou chaîne vide si null
 */
expect fun formatPlayDate(playDate: String?, isUtc: Boolean = true, offsetHours: Long = 0): String

/**
 * Retourne la date et l'heure actuelle formatée pour le partage.
 */
expect fun getCurrentFormattedDate(): String
