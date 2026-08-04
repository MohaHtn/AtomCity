package org.arcade.atomcity.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.arcade.atomcity.network.MaiteaProfileClient
import org.arcade.atomcity.network.ScorefetcherClient
import org.arcade.atomcity.worker.ImportWorkManager
import org.arcade.atomcity.utils.ApiKeyManager
import org.arcade.atomcity.db.AppDatabase
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse
import org.arcade.atomcity.model.maitea.playerBest30Response.PlayerBest30Response
import org.arcade.atomcity.model.maitea.ChartHistoryResponse
import org.arcade.atomcity.model.maitea.BestPerPlayerResponse
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaApiData
import org.arcade.atomcity.network.DeleteApiKeyResponse
import kotlinx.coroutines.flow.map

class MaiteaRepository(
    private val profileClient: MaiteaProfileClient,
    private val scorefetcherClient: ScorefetcherClient,
    private val importWorkManager: ImportWorkManager,
    private val apiKeyManager: ApiKeyManager,
    private val database: AppDatabase,
    private val scorefetcherApiKey: String
) {
    fun observeImportWorkerStatus(): Flow<ImportWorkerStatus?> = importWorkManager.observeProgress().map { progress ->
        progress?.let {
            ImportWorkerStatus(
                isActive = it.state == "running" || it.state == "enqueued",
                state = it.state,
                progress = it.progress,
                message = it.message ?: ""
            )
        }
    }

    fun setImportWorkerActive(active: Boolean) {
        // Logic to track active state if needed
    }

    fun isImportWorkerActive(): Boolean {
        return false
    }

    fun clearMaiTeaPaginatedCache() {
        // Cache clearing logic
    }

    suspend fun refreshImportWorkerStatus(): ImportWorkerStatus {
        return ImportWorkerStatus(false, "idle", 0, "")
    }

    fun startMaiTeaImport() {
        // Start worker logic
    }

    fun getMaiTeaPaginatedData(page: Int): Flow<MaiteaPlaysResponse?> = flow {
        // Should fetch from apiKeyManager then call client
        emit(null)
    }

    fun getMaiTeaPlayerDetails(): Flow<MaiteaPlayerDetailsResponse?> = flow {
        emit(null)
    }

    fun getProfiles(): Flow<Map<String, String>> = flow {
        emit(scorefetcherClient.getProfiles())
    }

    fun get30BestCharts(): Flow<List<PlayerBest30Response>> = flow {
        emit(emptyList())
    }

    fun getChartHistory(songName: String, difficulty: String?): Flow<List<ChartHistoryResponse>> = flow {
        emit(emptyList())
    }

    fun getBestPerPlayer(songName: String, difficulty: String?): Flow<List<BestPerPlayerResponse>> = flow {
        emit(emptyList())
    }

    fun getPlayById(id: Int, keyHash: String): Flow<MaiteaApiData?> = flow {
        emit(scorefetcherClient.getPlayById(id, keyHash))
    }

    fun searchCharts(query: String): Flow<List<BestPerPlayerResponse>> = flow {
        emit(scorefetcherClient.searchCharts(query))
    }
    
    suspend fun removeApiKey(apiKey: String): Flow<DeleteApiKeyResponse> = flow {
        emit(scorefetcherClient.deleteApiKey(apiKey))
    }
}
