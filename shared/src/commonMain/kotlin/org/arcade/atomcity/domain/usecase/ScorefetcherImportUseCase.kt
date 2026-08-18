package org.arcade.atomcity.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.data.ImportWorkerStatus
import org.arcade.atomcity.data.ScorefetcherRepository

class ScorefetcherImportUseCase(private val repository: ScorefetcherRepository) {
    fun observeImportWorkerStatus(): Flow<ImportWorkerStatus?> = repository.observeImportWorkerStatus()

    fun setImportWorkerActive(active: Boolean) = repository.setImportWorkerActive(active)

    suspend fun refreshImportWorkerStatus(): ImportWorkerStatus = repository.refreshImportWorkerStatus()

    suspend fun startScorefetcherImport(): Boolean = repository.startScorefetcherImport()

    fun clearScorefetcherPaginatedCache() = repository.clearScorefetcherPaginatedCache()
}
