/**
 * Import Worker Status Domain Model
 *
 * Represents the current status, state, progress, and messages of background data import workers.
 */
package org.arcade.atomcity.domain.model

data class ImportWorkerStatus(
    val isActive: Boolean,
    val state: String,
    val progress: Int,
    val message: String?
)
