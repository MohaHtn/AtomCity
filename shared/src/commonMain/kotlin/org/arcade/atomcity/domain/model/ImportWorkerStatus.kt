package org.arcade.atomcity.domain.model

data class ImportWorkerStatus(
    val isActive: Boolean,
    val state: String,
    val progress: Int,
    val message: String?
)
