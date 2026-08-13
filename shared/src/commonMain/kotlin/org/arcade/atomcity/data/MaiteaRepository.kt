package org.arcade.atomcity.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.arcade.atomcity.network.MaiteaProfileClient
import org.arcade.atomcity.network.ScorefetcherClient
import org.arcade.atomcity.network.ImportService
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
import org.arcade.atomcity.network.ApiKeyRequest
import org.arcade.atomcity.model.maitea.MaimaiMostPlayedEntry
import org.arcade.atomcity.data.cache.DataCache
import org.arcade.atomcity.utils.PlatformUtils
import kotlinx.coroutines.flow.map

class MaiteaRepository(
    private val profileClient: MaiteaProfileClient,
    private val scorefetcherClient: ScorefetcherClient,
    private val importService: ImportService,
    private val importWorkManager: ImportWorkManager,
    private val apiKeyManager: ApiKeyManager,
    private val database: AppDatabase,
    private val jacketImages: Map<String, String>,
    private val scorefetcherApiKey: String
) {
    private val playsCache = mutableMapOf<Int, DataCache<MaiteaPlaysResponse>>()
    private val playerDetailsCache = DataCache<MaiteaPlayerDetailsResponse>()

    fun findJacketUrlBySongName(songName: String?): String? = jacketImages[songName]

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
        // Implementation depends on how we want to track this in KMP
    }

    fun isImportWorkerActive(): Flow<Boolean> = importWorkManager.isImportActive()

    fun clearMaiTeaPaginatedCache() {
        playsCache.values.forEach { it.clear() }
    }

    suspend fun refreshImportWorkerStatus(): ImportWorkerStatus {
        // In KMP, we might rely more on the flow provided by ImportWorkManager
        return ImportWorkerStatus(false, "idle", 0, "")
    }

    suspend fun startMaiTeaImport(): Boolean {
        val apiKey = apiKeyManager.getApiKey("maimai")?.trim() ?: return false
        if (apiKey.isBlank()) return false

        return try {
            val keyCheck = scorefetcherClient.checkApiKey(apiKey)
            if (!keyCheck.isKeyProvidedInDatabase) {
                scorefetcherClient.addApiKey(
                    ApiKeyRequest(key = apiKey, description = "MaiTea Key (KMP)")
                )
            }
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

        try {
            val response = scorefetcherClient.getScores("Bearer ${apiKey.trim()}", page.toString())
            response.data.forEach { play ->
                play.jacketImageUrl = findJacketUrlBySongName(play.song?.name?.jp) ?: findJacketUrlBySongName(play.song?.name?.en)
            }
            pageCache.put(response)
            emit(response)
        } catch (e: Exception) {
            PlatformUtils.log("MaiteaRepository", "Error fetching scores from scorefetcher: ${e.message}", true)
            emit(null)
        }
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

        try {
            val response = profileClient.getPlayerDetails(apiKey.trim())
            playerDetailsCache.put(response)
            emit(response)
        } catch (e: Exception) {
            PlatformUtils.log("MaiteaRepository", "Error fetching player details: ${e.message}", true)
            emit(null)
        }
    }

    fun getProfiles(): Flow<Map<String, String>> = flow {
        try {
            emit(scorefetcherClient.getProfiles())
        } catch (e: Exception) {
            emit(emptyMap())
        }
    }

    fun get30BestCharts(hashKey: String? = null): Flow<List<PlayerBest30Response>> = flow {
        try {
            val key = hashKey ?: PlatformUtils.sha256(apiKeyManager.getApiKey("maimai")?.trim() ?: "")
            if (key.isBlank()) {
                emit(emptyList())
                return@flow
            }
            val response = scorefetcherClient.get30BestCharts(key)
            response.forEach { entry ->
                entry.jacketImageUrl = findJacketUrlBySongName(entry.songJson?.name?.jp) ?: findJacketUrlBySongName(entry.songJson?.name?.en)
            }
            emit(response)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    fun getChartHistory(songName: String, difficulty: String?): Flow<List<ChartHistoryResponse>> = flow {
        try {
            val key = PlatformUtils.sha256(apiKeyManager.getApiKey("maimai")?.trim() ?: "")
            if (key.isBlank()) {
                emit(emptyList())
                return@flow
            }

            val realDiff = if (!difficulty.isNullOrBlank()) {
                when (difficulty.trim().lowercase()) {
                    "basic" -> "Basic"
                    "advanced" -> "Advanced"
                    "expert" -> "Expert"
                    "master" -> "Master"
                    "remaster" -> "Re:Master"
                    "utage" -> "宴"
                    else -> difficulty
                }
            } else null

            emit(scorefetcherClient.getChartHistory(key, songName, realDiff))
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    fun getBestPerPlayer(songName: String, difficulty: String?): Flow<List<BestPerPlayerResponse>> = flow {
        try {
            val response = scorefetcherClient.getBestPerPlayer(songName, difficulty)
            response.forEach { entry ->
                entry.jacketImageUrl = findJacketUrlBySongName(entry.songJson?.name?.jp) ?: findJacketUrlBySongName(entry.songJson?.name?.en)
            }
            emit(response)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    fun getPlayById(id: Int, keyHash: String): Flow<MaiteaApiData?> = flow {
        try {
            val play = scorefetcherClient.getPlayById(id, keyHash)
            play.jacketImageUrl = findJacketUrlBySongName(play.song?.name?.jp) ?: findJacketUrlBySongName(play.song?.name?.en)
            emit(play)
        } catch (e: Exception) {
            PlatformUtils.log("MaiteaRepository", "Error fetching play by id $id: ${e.message}", true)
            emit(null)
        }
    }

    fun searchCharts(query: String, keyHash: String? = null): Flow<List<BestPerPlayerResponse>> = flow {
        try {
            val key = keyHash ?: PlatformUtils.sha256(apiKeyManager.getApiKey("maimai")?.trim() ?: "")
            val response = scorefetcherClient.searchCharts(query, key)
            response.forEach { entry ->
                entry.jacketImageUrl = findJacketUrlBySongName(entry.songJson?.name?.jp) ?: findJacketUrlBySongName(entry.songJson?.name?.en)
            }
            emit(response)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    fun getMostPlayed(
        limit: Int? = 30,
        period: String? = "month",
        date: String? = null
    ): Flow<List<MaimaiMostPlayedEntry>> = flow {
        try {
            val response = when (period?.lowercase()) {
                "day" -> scorefetcherClient.getMostPlayed(limit = limit, day = date)
                "week" -> scorefetcherClient.getMostPlayed(limit = limit, week = date)
                "month" -> scorefetcherClient.getMostPlayed(limit = limit, month = date)
                else -> scorefetcherClient.getMostPlayed(limit = limit, period = period, date = date)
            }
            response.forEach { entry ->
                entry.jacketImageUrl = entry.songJson?.name?.jp?.let { findJacketUrlBySongName(it) }
                    ?: entry.songJson?.name?.en?.let { findJacketUrlBySongName(it) }
                    ?: entry.songName?.let { findJacketUrlBySongName(it) }
            }
            emit(response)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    fun getMostPlayedByHash(
        keyHash: String? = null,
        limit: Int? = 30,
        period: String? = "month",
        date: String? = null
    ): Flow<List<MaimaiMostPlayedEntry>> = flow {
        try {
            val key = keyHash ?: PlatformUtils.sha256(apiKeyManager.getApiKey("maimai")?.trim() ?: "")
            if (key.isBlank()) {
                emit(emptyList())
                return@flow
            }
            val response = when (period?.lowercase()) {
                "day" -> scorefetcherClient.getMostPlayedByHash(keyHash = key, limit = limit, day = date)
                "week" -> scorefetcherClient.getMostPlayedByHash(keyHash = key, limit = limit, week = date)
                "month" -> scorefetcherClient.getMostPlayedByHash(keyHash = key, limit = limit, month = date)
                else -> scorefetcherClient.getMostPlayedByHash(keyHash = key, limit = limit, period = period, date = date)
            }
            response.forEach { entry ->
                entry.jacketImageUrl = entry.songJson?.name?.jp?.let { findJacketUrlBySongName(it) }
                    ?: entry.songJson?.name?.en?.let { findJacketUrlBySongName(it) }
                    ?: entry.songName?.let { findJacketUrlBySongName(it) }
            }
            emit(response)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    suspend fun removeApiKey(apiKey: String): Flow<DeleteApiKeyResponse> = flow {
        try {
            val keyHash = PlatformUtils.sha256(apiKey.trim())
            emit(scorefetcherClient.deleteApiKey(keyHash))
        } catch (e: Exception) {
            // Handle error appropriately
        }
    }
}
