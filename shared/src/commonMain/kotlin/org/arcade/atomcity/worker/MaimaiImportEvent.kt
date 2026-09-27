/**
 * Maimai Import Event Model
 *
 * Data model representing server-sent events (SSE) during Maimai score data imports.
 */
package org.arcade.atomcity.worker

import kotlinx.serialization.Serializable

@Serializable
data class MaimaiImportEvent(
    val type: String,
    val keyHash: String,
    val page: Int? = null,
    val totalPages: Int? = null,
    val message: String? = null
)
