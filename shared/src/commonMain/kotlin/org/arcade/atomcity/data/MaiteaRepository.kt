package org.arcade.atomcity.data

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.HttpStatement
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.arcade.atomcity.data.cache.DataCache
import org.arcade.atomcity.model.maitea.BestPerPlayerResponse
import org.arcade.atomcity.model.maitea.ChartHistoryResponse
import org.arcade.atomcity.model.maitea.playerBest30Response.PlayerBest30Response
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaApiData
import org.arcade.atomcity.network.ApiKeyRequest
import org.arcade.atomcity.network.DeleteApiKeyResponse
import org.arcade.atomcity.network.MaiteaProfileClient
import org.arcade.atomcity.network.ScorefetcherClient
import org.arcade.atomcity.utils.ApiKeyManager
import org.arcade.atomcity.utils.PlatformUtils
import org.arcade.atomcity.worker.ImportProgress
import org.arcade.atomcity.worker.ImportWorkManager
import org.arcade.atomcity.worker.MaimaiImportEvent
import kotlin.concurrent.Volatile

class MaiteaRepository(
    private val scorefetcherClient: ScorefetcherClient,
    private val apiKeyManager: ApiKeyManager,
    private val maiteaProfileClient: MaiteaProfileClient,
    private val importWorkManager: ImportWorkManager,
    private val httpClient: HttpClient,
    private val scorefetcherApiKey: String
) {
    companion object {
        private const val IMPORT_EVENTS_URL = "https://scorefetcher.mohahtn.xyz/imports"
    }

    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var localImportWorkerActive = false

    // Cache for paginated data
    private val playsCache = mutableMapOf<Int, DataCache<MaiteaPlaysResponse>>()

    // Cache for player details
    private val playerDetailsCache = DataCache<MaiteaPlayerDetailsResponse>()

    fun clearMaiTeaPaginatedCache() {
        playsCache.values.forEach { it.clear() }
    }

    suspend fun startMaiTeaImport(): Boolean = withContext(Dispatchers.IO) {
        if (localImportWorkerActive) return@withContext true
        
        val apiKey = apiKeyManager.getApiKey("maimai") ?: ""
        if (apiKey.isBlank()) return@withContext false

        try {
            val keyCheck = scorefetcherClient.checkApiKey(apiKey)
            if (!keyCheck.isKeyProvidedInDatabase) {
                scorefetcherClient.addApiKey(
                    ApiKeyRequest(key = apiKey, description = "MaiTea Key (App)")
                )
            }
            
            localImportWorkerActive = true
            importWorkManager.startImport(apiKey)
            true
        } catch (e: Exception) {
            PlatformUtils.log("MaiteaRepository", "Error starting MaiTea import: ${e.message}", true)
            false
        }
    }

    fun getMaiTeaPaginatedData(page: Int): Flow<MaiteaPlaysResponse?> = flow {
        val pageCache = playsCache.getOrPut(page) { DataCache() }
        pageCache.get()?.let {
            emit(it)
            return@flow
        }

        val apiKey = apiKeyManager.getApiKey("maimai") ?: ""
        if (apiKey.isBlank()) {
            emit(null)
            return@flow
        }

        val keyCheck = scorefetcherClient.checkApiKey(apiKey)
        if (keyCheck.isKeyProvidedInDatabase) {
            getScores(apiKey, page.toString()).collect { response ->
                response?.let { pageCache.put(it) }
                emit(response)
            }
        } else {
            emit(null)
        }
    }

    fun setImportWorkerActive(active: Boolean) {
        localImportWorkerActive = active
    }

    fun getImportWorkerActive(): Boolean {
        return localImportWorkerActive
    }

    fun observeImportWorkerStatus(): Flow<ImportProgress?> {
        return importWorkManager.observeProgress()
    }

    fun observeRemoteImportStatus(keyHash: String): Flow<MaimaiImportEvent?> = flow {
        try {
            httpClient.prepareGet("$IMPORT_EVENTS_URL/$keyHash") {
                header("X-API-KEY", scorefetcherApiKey)
                header("Accept", "text/event-stream")
                header("Cache-Control", "no-cache")
                header("Connection", "keep-alive")
            }.execute { response ->
                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break
                    if (line.startsWith("data:")) {
                        val data = line.removePrefix("data:").trim()
                        if (data.isNotEmpty()) {
                            try {
                                val event = json.decodeFromString<MaimaiImportEvent>(data)
                                emit(event)
                            } catch (e: Exception) {
                                PlatformUtils.log("MaiteaRepository", "Error decoding SSE: ${e.message}", true)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            PlatformUtils.log("MaiteaRepository", "SSE connection error: ${e.message}", true)
            emit(null)
        }
    }

    suspend fun refreshImportWorkerStatus(): ImportWorkerStatus = withContext(Dispatchers.IO) {
        if (localImportWorkerActive) {
            return@withContext ImportWorkerStatus(true, "running", 0, "Initialisation de l'importation...")
        }

        val apiKey = apiKeyManager.getApiKey("maimai") ?: ""
        if (apiKey.isBlank()) return@withContext ImportWorkerStatus(false, "idle", 0, null)

        val keyHash = PlatformUtils.sha256(apiKey)
        
        try {
            // We'll use a manual stream reading here since we don't have a full SSE library yet in shared
            // This is a simplified version of what was there with OkHttp
            // TODO: Use a proper SSE client for KMP
            ImportWorkerStatus(false, "idle", 0, null) 
        } catch (_: Exception) {
            ImportWorkerStatus(false, "idle", 0, null)
        }
    }

    suspend fun isImportWorkerActive(): Boolean = withContext(Dispatchers.IO) {
        if (localImportWorkerActive) return@withContext true

        // Check local work manager state if possible
        // importWorkManager.isImportActive() is a Flow, we want a snapshot
        // For now, let's just rely on a simple check or the remote status
        val remoteStatus = refreshImportWorkerStatus()
        remoteStatus.isActive
    }

    fun getScores(token: String, page: String): Flow<MaiteaPlaysResponse?> = flow {
        val response = try {
            scorefetcherClient.getScores(token = "Bearer $token", pageNumber = page)
        } catch (e: Exception) {
            maiteaProfileClient.getAllUserScores(token = token, page = page.toInt())
        }
        emit(response)
    }

    fun removeApiKey(apiKey: String): Flow<DeleteApiKeyResponse> = flow {
        val keyHash = PlatformUtils.sha256(apiKey)
        val response = scorefetcherClient.deleteApiKey(keyHash = keyHash)
        emit(response)
    }

    fun getMaiTeaPlayerDetails(): Flow<MaiteaPlayerDetailsResponse?> = flow {
        playerDetailsCache.get()?.let {
            emit(it)
            return@flow
        }
        val apiKey = apiKeyManager.getApiKey("maimai") ?: ""
        if (apiKey.isBlank()) {
            emit(null)
            return@flow
        }
        val response = maiteaProfileClient.getPlayerDetails(apiKey)
        playerDetailsCache.put(response)
        emit(response)
    }

    fun getProfiles(): Flow<Map<String, String>> = flow {
        emit(scorefetcherClient.getProfiles())
    }

    fun get30BestCharts(hashKey: String? = null): Flow<List<PlayerBest30Response>> = flow {
        try {
            val key = hashKey ?: PlatformUtils.sha256(apiKeyManager.getApiKey("maimai") ?: "")
            if (key.isBlank()) {
                emit(emptyList())
                return@flow
            }
            val response = scorefetcherClient.get30BestCharts(key)
            emit(response)
        } catch (e: Exception) {
            PlatformUtils.log("MaiteaRepository", "Error fetching best 30 charts: ${e.message}", true)
            emit(emptyList())
        }
    }

    fun getChartHistory(songName: String, difficulty: String? = null): Flow<List<ChartHistoryResponse>> = flow {
        try {
            val key = PlatformUtils.sha256(apiKeyManager.getApiKey("maimai") ?: "")
            if (key.isBlank()) {
                emit(emptyList())
                return@flow
            }

            val realDiff = if (!difficulty.isNullOrBlank()) {
                when (difficulty.trim().lowercase()) {
                    "remaster" -> "Re:Master"
                    "utage" -> "宴"
                    else -> difficulty
                }
            } else null

            val response = scorefetcherClient.getChartHistory(key, songName, realDiff)
            emit(response)
        } catch (e: Exception) {
            PlatformUtils.log("MaiteaRepository", "Error fetching chart history: ${e.message}", true)
            emit(emptyList())
        }
    }

    fun getBestPerPlayer(songName: String, difficulty: String? = null): Flow<List<BestPerPlayerResponse>> = flow {
        try {
            val response = scorefetcherClient.getBestPerPlayer(songName, difficulty)
            emit(response)
        } catch (e: Exception) {
            PlatformUtils.log("MaiteaRepository", "Error fetching best-per-player: ${e.message}", true)
            emit(emptyList())
        }
    }

    fun getPlayById(id: Int, keyHash: String): Flow<MaiteaApiData?> = flow {
        try {
            val response = scorefetcherClient.getPlayById(id, keyHash)
            emit(response)
        } catch (e: Exception) {
            PlatformUtils.log("MaiteaRepository", "Error fetching play by ID: ${e.message}", true)
            emit(null)
        }
    }

    fun searchCharts(query: String, keyHash: String? = null): Flow<List<BestPerPlayerResponse>> = flow {
        try {
            val key = keyHash ?: PlatformUtils.sha256(apiKeyManager.getApiKey("maimai") ?: "")
            val response = scorefetcherClient.searchCharts(query, key)
            emit(response)
        } catch (e: Exception) {
            PlatformUtils.log("MaiteaRepository", "Error searching charts: ${e.message}", true)
            emit(emptyList())
        }
    }
}

data class ImportWorkerStatus(
    val isActive: Boolean,
    val state: String,
    val progress: Int,
    val message: String?
)
