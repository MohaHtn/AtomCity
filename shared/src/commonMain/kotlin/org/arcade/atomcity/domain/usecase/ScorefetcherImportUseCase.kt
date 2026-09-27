/**
 * Scorefetcher Import Use Case
 *
 * Encapsulates business logic for managing background score import tasks and worker status.
 */
package org.arcade.atomcity.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.domain.model.ImportWorkerStatus
import org.arcade.atomcity.domain.repository.IScorefetcherRepository

/**
 * Use case responsible for managing the background score import worker and its status.
 * 
 * Adheres to the Single Responsibility Principle (SRP) by isolating import orchestration logic.
 *
 * @property repository The [IScorefetcherRepository] data source.
 */
class ScorefetcherImportUseCase(private val repository: IScorefetcherRepository) {
    /**
     * Observes the progress and status of the import worker.
     *
     * @return A [Flow] emitting the current [ImportWorkerStatus], or null.
     */
    fun observeImportWorkerStatus(): Flow<ImportWorkerStatus?> = repository.observeImportWorkerStatus()

    /**
     * Sets the active state of the import worker.
     *
     * @param active True if the worker is active, false otherwise.
     */
    fun setImportWorkerActive(active: Boolean) = repository.setImportWorkerActive(active)

    /**
     * Refreshes and fetches the import worker status.
     *
     * @return The updated [ImportWorkerStatus].
     */
    suspend fun refreshImportWorkerStatus(): ImportWorkerStatus = repository.refreshImportWorkerStatus()

    /**
     * Starts the score import process.
     *
     * @return True if the import was successfully initiated, false otherwise.
     */
    suspend fun startScorefetcherImport(): Boolean = repository.startScorefetcherImport()

    /**
     * Clears the cached paginated data related to scores.
     */
    fun clearScorefetcherPaginatedCache() = repository.clearScorefetcherPaginatedCache()
}
