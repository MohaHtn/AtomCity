package org.arcade.atomcity.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.domain.model.ImportWorkerStatus
import org.arcade.atomcity.domain.repository.IScorefetcherRepository

class ScorefetcherImportUseCase(private val repository: IScorefetcherRepository) {
    fun observeImportWorkerStatus(): Flow<ImportWorkerStatus?> = repository.observeImportWorkerStatus()

    fun setImportWorkerActive(active: Boolean) = repository.setImportWorkerActive(active)

    suspend fun refreshImportWorkerStatus(): ImportWorkerStatus = repository.refreshImportWorkerStatus()

    suspend fun startScorefetcherImport(): Boolean = repository.startScorefetcherImport()

    fun clearScorefetcherPaginatedCache() = repository.clearScorefetcherPaginatedCache()
}
