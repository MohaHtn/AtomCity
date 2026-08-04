package org.arcade.atomcity.worker

import kotlinx.coroutines.flow.Flow

interface ImportWorkManager {
    fun startImport(apiKey: String)
    fun isImportActive(): Flow<Boolean>
    fun observeProgress(): Flow<ImportProgress?>
}

data class ImportProgress(
    val state: String,
    val progress: Int,
    val message: String?
)
