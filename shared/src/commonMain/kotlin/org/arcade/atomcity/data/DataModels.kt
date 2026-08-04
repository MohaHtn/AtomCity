package org.arcade.atomcity.data

data class ImportWorkerStatus(
    val isActive: Boolean,
    val state: String,
    val progress: Int,
    val message: String
)

data class LevelInfo(
    val level: String = "",
    val internalLevel: String = ""
)
