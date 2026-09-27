/**
 * Level Information Domain Model
 *
 * Represents chart level and internal level rating details for song difficulties.
 */
package org.arcade.atomcity.domain.model

data class LevelInfo(
    val level: String = "",
    val internalLevel: String = ""
)
