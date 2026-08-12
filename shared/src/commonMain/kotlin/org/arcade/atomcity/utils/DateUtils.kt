package org.arcade.atomcity.utils

/**
 * Formate une date ISO en format lisible.
 * @param playDate la date au format ISO à formater (peut être null)
 * @return une chaîne formatée "jour mois année, heure:minute" ou chaîne vide si null
 */
expect fun formatPlayDate(playDate: String?): String
