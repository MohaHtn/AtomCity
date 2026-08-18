package org.arcade.atomcity.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.arcade.atomcity.data.remote.ScorefetcherProfileClient
import org.arcade.atomcity.data.remote.ScorefetcherClient
import org.arcade.atomcity.data.remote.ImportService
import org.arcade.atomcity.worker.ImportWorkManager
import org.arcade.atomcity.utils.ApiKeyManager
import org.arcade.atomcity.data.local.AppDatabase
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherPlaysResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.playerDetailsResponse.ScorefetcherPlayerDetailsResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.playerBest30Response.PlayerBest30Response
import org.arcade.atomcity.data.remote.model.scorefetcher.ChartHistoryResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.BestPerPlayerResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherApiData
import org.arcade.atomcity.data.remote.DeleteApiKeyResponse
import org.arcade.atomcity.data.remote.ApiKeyRequest
import org.arcade.atomcity.data.remote.model.scorefetcher.MaimaiMostPlayedEntry
import org.arcade.atomcity.data.cache.DataCache
import org.arcade.atomcity.utils.PlatformUtils
import kotlinx.coroutines.flow.map
import org.arcade.atomcity.domain.repository.IDifficultyRepository
import org.arcade.atomcity.domain.repository.IScorefetcherRepository
import org.arcade.atomcity.domain.model.ImportWorkerStatus
import org.arcade.atomcity.domain.model.LevelInfo

class ScorefetcherRepository(
    private val profileClient: ScorefetcherProfileClient,
    private val scorefetcherClient: ScorefetcherClient,
    private val importService: ImportService,
    private val importWorkManager: ImportWorkManager,
    private val apiKeyManager: ApiKeyManager,
    private val database: AppDatabase,
    private val difficultyRepository: IDifficultyRepository,
    private val jacketImages: Map<String, String>,
    private val scorefetcherApiKey: String
) : IScorefetcherRepository {
    private val playsCache = mutableMapOf<Int, DataCache<ScorefetcherPlaysResponse>>()
    private val playerDetailsCache = DataCache<ScorefetcherPlayerDetailsResponse>()

    override fun findJacketUrlBySongName(songName: String?): String? = jacketImages[songName]

    override fun observeImportWorkerStatus(): Flow<ImportWorkerStatus?> = importWorkManager.observeProgress().map { progress ->
        progress?.let {
            ImportWorkerStatus(
                isActive = it.state == "running" || it.state == "enqueued",
                state = it.state,
                progress = it.progress,
                message = it.message ?: ""
            )
        }
    }

    override fun setImportWorkerActive(active: Boolean) {
        // Implementation depends on how we want to track this in KMP
    }

    override fun isImportWorkerActive(): Flow<Boolean> = importWorkManager.isImportActive()

    override fun clearScorefetcherPaginatedCache() {
        playsCache.values.forEach { it.clear() }
    }

    override suspend fun refreshImportWorkerStatus(): ImportWorkerStatus {
        // In KMP, we might rely more on the flow provided by ImportWorkManager
        return ImportWorkerStatus(false, "idle", 0, "")
    }

    override suspend fun startScorefetcherImport(): Boolean {
        val apiKey = apiKeyManager.getApiKey("maimai")?.trim() ?: return false
        if (apiKey.isBlank()) return false

        return try {
            val keyCheck = scorefetcherClient.checkApiKey(apiKey)
            if (!keyCheck.isKeyProvidedInDatabase) {
                scorefetcherClient.addApiKey(
                    ApiKeyRequest(key = apiKey, description = "Scorefetcher Key (KMP)")
                )
            }
            importWorkManager.startImport(apiKey)
            true
        } catch (e: Exception) {
            PlatformUtils.log("ScorefetcherRepository", "Error starting Scorefetcher import: ${e.message}", true)
            false
        }
    }

    override fun getScorefetcherPaginatedData(page: Int): Flow<ScorefetcherPlaysResponse?> = flow {
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
            PlatformUtils.log("ScorefetcherRepository", "Error fetching scores from scorefetcher: ${e.message}", true)
            emit(null)
        }
    }

    override fun getScorefetcherPlayerDetails(): Flow<ScorefetcherPlayerDetailsResponse?> = flow {
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
            PlatformUtils.log("ScorefetcherRepository", "Error fetching player details: ${e.message}", true)
            emit(null)
        }
    }

    override fun getProfiles(): Flow<Map<String, String?>> = flow {
        try {
            emit(scorefetcherClient.getProfiles())
        } catch (e: Exception) {
            emit(emptyMap())
        }
    }

    override fun getRatings(): Flow<Map<String, Int?>> = flow {
        try {
            emit(scorefetcherClient.getRatings())
        } catch (e: Exception) {
            emit(emptyMap())
        }
    }

    override fun get30BestCharts(hashKey: String?): Flow<List<PlayerBest30Response>> = flow {
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

    override fun getChartHistory(songName: String, difficulty: String?): Flow<List<ChartHistoryResponse>> = flow {
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

    override fun getBestPerPlayer(songName: String, difficulty: String?): Flow<List<BestPerPlayerResponse>> = flow {
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

    override fun getPlayById(id: Int, keyHash: String): Flow<ScorefetcherApiData?> = flow {
        try {
            val play = scorefetcherClient.getPlayById(id, keyHash)
            play.jacketImageUrl = findJacketUrlBySongName(play.song?.name?.jp) ?: findJacketUrlBySongName(play.song?.name?.en)
            emit(play)
        } catch (e: Exception) {
            PlatformUtils.log("ScorefetcherRepository", "Error fetching play by id $id: ${e.message}", true)
            emit(null)
        }
    }

    override fun searchCharts(query: String, keyHash: String?): Flow<List<BestPerPlayerResponse>> = flow {
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

    override fun getMostPlayed(
        limit: Int?,
        period: String?,
        date: String?,
        groupByHashkey: Boolean
    ): Flow<List<MaimaiMostPlayedEntry>> = flow {
        try {
            val response = when (period?.lowercase()) {
                "day" -> scorefetcherClient.getMostPlayed(limit = limit, day = date, groupByHashkey = groupByHashkey)
                "week" -> scorefetcherClient.getMostPlayed(limit = limit, week = date, groupByHashkey = groupByHashkey)
                "month" -> scorefetcherClient.getMostPlayed(limit = limit, month = date, groupByHashkey = groupByHashkey)
                "alltime" -> scorefetcherClient.getMostPlayed(limit = limit, alltime = "true", groupByHashkey = groupByHashkey)
                else -> scorefetcherClient.getMostPlayed(limit = limit, period = period, date = date, groupByHashkey = groupByHashkey)
            }
            response.forEach { entry ->
                entry.jacketImageUrl = entry.songNameJp?.let { findJacketUrlBySongName(it) }
                    ?: entry.songJson?.name?.jp?.let { findJacketUrlBySongName(it) }
                    ?: entry.songNameEn?.let { findJacketUrlBySongName(it) }
                    ?: entry.songJson?.name?.en?.let { findJacketUrlBySongName(it) }
                    ?: entry.songName?.let { findJacketUrlBySongName(it) }

                // Fetch level info
                val diffIndex = org.arcade.atomcity.ui.game.maimai.getDifficultyIndex(entry.difficulty)
                if (diffIndex != -1) {
                    val songId = entry.songJson?.id ?: -1
                    entry.levelInfo = difficultyRepository.getLevelByDifficulty(
                        songId = songId,
                        diffIndex = diffIndex,
                        songTitle = entry.songNameJp ?: entry.songName,
                        altTitle = entry.songNameEn
                    )
                }
            }
            emit(response)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override fun getMostPlayedByHash(
        keyHash: String?,
        limit: Int?,
        period: String?,
        date: String?,
        groupByHashkey: Boolean
    ): Flow<List<MaimaiMostPlayedEntry>> = flow {
        try {
            val key = keyHash ?: PlatformUtils.sha256(apiKeyManager.getApiKey("maimai")?.trim() ?: "")
            if (key.isBlank()) {
                emit(emptyList())
                return@flow
            }
            val response = when (period?.lowercase()) {
                "day" -> scorefetcherClient.getMostPlayedByHash(keyHash = key, limit = limit, day = date, groupByHashkey = groupByHashkey)
                "week" -> scorefetcherClient.getMostPlayedByHash(keyHash = key, limit = limit, week = date, groupByHashkey = groupByHashkey)
                "month" -> scorefetcherClient.getMostPlayedByHash(keyHash = key, limit = limit, month = date, groupByHashkey = groupByHashkey)
                "alltime" -> scorefetcherClient.getMostPlayedByHash(keyHash = key, limit = limit, alltime = "true", groupByHashkey = groupByHashkey)
                else -> scorefetcherClient.getMostPlayedByHash(keyHash = key, limit = limit, period = period, date = date, groupByHashkey = groupByHashkey)
            }
            response.forEach { entry ->
                entry.jacketImageUrl = entry.songNameJp?.let { findJacketUrlBySongName(it) }
                    ?: entry.songJson?.name?.jp?.let { findJacketUrlBySongName(it) }
                    ?: entry.songNameEn?.let { findJacketUrlBySongName(it) }
                    ?: entry.songJson?.name?.en?.let { findJacketUrlBySongName(it) }
                    ?: entry.songName?.let { findJacketUrlBySongName(it) }

                // Fetch level info
                val diffIndex = org.arcade.atomcity.ui.game.maimai.getDifficultyIndex(entry.difficulty)
                if (diffIndex != -1) {
                    val songId = entry.songJson?.id ?: -1
                    entry.levelInfo = difficultyRepository.getLevelByDifficulty(
                        songId = songId,
                        diffIndex = diffIndex,
                        songTitle = entry.songNameJp ?: entry.songName,
                        altTitle = entry.songNameEn
                    )
                }
            }
            emit(response)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun removeApiKey(apiKey: String): Flow<DeleteApiKeyResponse> = flow {
        try {
            val keyHash = PlatformUtils.sha256(apiKey.trim())
            emit(scorefetcherClient.deleteApiKey(keyHash))
        } catch (e: Exception) {
            // Handle error appropriately
        }
    }
}
