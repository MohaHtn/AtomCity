
package org.arcade.atomcity.data

import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.arcade.atomcity.BuildConfig
import org.arcade.atomcity.data.cache.DataCache
import org.arcade.atomcity.model.maitea.BestPerPlayerResponse
import org.arcade.atomcity.model.maitea.ChartHistoryResponse
import org.arcade.atomcity.model.maitea.playerBest30Response.PlayerBest30Response
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaApiData
import org.arcade.atomcity.network.ApiKeyRequest
import org.arcade.atomcity.network.DeleteApiKeyResponse
import org.arcade.atomcity.network.ErrorInterceptor
import org.arcade.atomcity.network.MaiteaProfileService
import org.arcade.atomcity.network.ScorefetcherService
import org.arcade.atomcity.ui.core.GlobalUIState
import org.arcade.atomcity.utils.ApiKeyManager
import org.arcade.atomcity.worker.MaimaiImportEvent
import org.arcade.atomcity.worker.MaimaiImportWorker
import retrofit2.HttpException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class MaiteaRepository(
    private val scorefetcherService: ScorefetcherService,
    private val apiKeyManager: ApiKeyManager,
    private val maiteaProfileService: MaiteaProfileService,
    private val workManager: WorkManager
) {
   companion object {
       private const val IMPORT_SCORE_WORK = "maimai_import_work"
       private const val IMPORT_EVENTS_URL = "https://scorefetcher.mohahtn.xyz/imports"
   }

   private val importStreamClient = OkHttpClient.Builder()
       .addInterceptor(ErrorInterceptor())
       .addInterceptor(HttpLoggingInterceptor().apply {
           level = HttpLoggingInterceptor.Level.BASIC
       })
       .readTimeout(10, TimeUnit.SECONDS)
       .build()
   @Volatile
   private var localImportWorkerActive = false
   private val importEventAdapter = Moshi.Builder()
       .add(KotlinJsonAdapterFactory())
       .build()
       .adapter(MaimaiImportEvent::class.java)
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
            val keyCheck = scorefetcherService.checkApiKey(apiKey)
            if (!keyCheck.isKeyProvidedInDatabase) {
                scorefetcherService.addApiKey(
                    ApiKeyRequest(key = apiKey, description = "MaiTea Key (App)")
                )
            }
            
            localImportWorkerActive = true
            startImportWorker(apiKey)
            true
        } catch (e: Exception) {
            Log.e("MaiteaRepository", "Error starting MaiTea import: ${e.message}")
            false
        }
    }

    fun getMaiTeaPaginatedData(page: Int): Flow<MaiteaPlaysResponse?> = flow {
        // Get or create cache for this page
        val pageCache = playsCache.getOrPut(page) { DataCache() }

        // Check if data is in cache
        pageCache.get()?.let {
            emit(it)
            return@flow
        }

        // If not, make API call
        val apiKey = apiKeyManager.getApiKey("maimai") ?: ""

        if (apiKey.isBlank()) {
            emit(null)
            return@flow
        }

        val keyCheck = scorefetcherService.checkApiKey(apiKey)
        if (keyCheck.isKeyProvidedInDatabase) {
            getScores(apiKey, page.toString()).collect { response ->
                response?.let {
                    // Store data in cache
                    pageCache.put(it)
                }
                emit(response)
            }
        } else {
            emit(null)
        }
    }

    fun observeImportWorkerStatus(): Flow<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkFlow(IMPORT_SCORE_WORK)
    }

    fun setImportWorkerActive(active: Boolean) {
        localImportWorkerActive = active
    }

    fun getImportWorkerActive(): Boolean {
        return localImportWorkerActive
    }

    suspend fun refreshImportWorkerStatus(): ImportWorkerStatus = withContext(Dispatchers.IO) {
        if (localImportWorkerActive) {
            return@withContext ImportWorkerStatus(true, "running", 0, "Initialisation de l'importation...")
        }

        val apiKey = apiKeyManager.getApiKey("maimai") ?: ""
        if (apiKey.isBlank()) return@withContext ImportWorkerStatus(false, "idle", 0, null)

        val keyHash = sha256Hex(apiKey)
        val request = Request.Builder()
            .url("$IMPORT_EVENTS_URL/$keyHash/events")
            .addHeader("X-API-KEY", BuildConfig.SCOREFETCHER_API_KEY)
            .addHeader("Accept", "text/event-stream")
            .build()

        try {
            // Short timeout for status check to avoid blocking
            importStreamClient.newBuilder()
                .readTimeout(3, TimeUnit.SECONDS)
                .build()
                .newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext ImportWorkerStatus(false, "idle", 0, null)

                val body = response.body ?: return@withContext ImportWorkerStatus(false, "idle", 0, null)
                val source = body.source()

                // Look for current progress in the stream
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break
                    if (line.startsWith("data:")) {
                        val jsonData = line.removePrefix("data:").trim()
                        val event = try { importEventAdapter.fromJson(jsonData) } catch (_: Exception) { null }

                        if (event != null) {
                            when (event.type) {
                                "page" -> {
                                    val progress = if ((event.totalPages ?: 0) > 0) {
                                        (((event.page ?: 0).toFloat() / (event.totalPages ?: 1)) * 100).toInt().coerceIn(0, 100)
                                    } else 0
                                    return@withContext ImportWorkerStatus(true, "running", progress, "Importation en cours : $progress%")
                                }
                                "completed" -> return@withContext ImportWorkerStatus(false, "idle", 100, "Importation terminée")
                                "failed" -> return@withContext ImportWorkerStatus(false, "failed", 0, "Échec de l'importation")
                            }
                        }
                    }
                }
                ImportWorkerStatus(false, "idle", 0, null)
            }
        } catch (_: Exception) {
            ImportWorkerStatus(false, "idle", 0, null)
        }
    }

    suspend fun isImportWorkerActive(): Boolean = withContext(Dispatchers.IO) {
        if (localImportWorkerActive) {
            return@withContext true
        }

        try {
            val workInfos = workManager.getWorkInfosForUniqueWork(IMPORT_SCORE_WORK).get()
            val latestWork = workInfos.lastOrNull()
            val workManagerActive = latestWork?.state == WorkInfo.State.ENQUEUED ||
                latestWork?.state == WorkInfo.State.RUNNING ||
                latestWork?.state == WorkInfo.State.BLOCKED

            if (workManagerActive) {
                return@withContext true
            }
        } catch (_: Exception) {
            // Fall back to remote status below if WorkManager is unavailable.
        }

        val remoteStatus = refreshImportWorkerStatus()
        remoteStatus.isActive

    }

    private fun startImportWorker(apiKey: String) {
        val workRequest = OneTimeWorkRequestBuilder<MaimaiImportWorker>()
            .setInputData(MaimaiImportWorker.createInputData(apiKey))
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10L,
                TimeUnit.SECONDS
            )
            .build()
        workManager.enqueueUniqueWork(
            IMPORT_SCORE_WORK,
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

  fun getScores(token: String, page: String): Flow<MaiteaPlaysResponse?> = flow {
      val response = try {
          scorefetcherService.getScores(token = "Bearer $token", pageNumber = page)
      } catch (e: HttpException) {
          if (e.code() == 404) {
              maiteaProfileService.getAllUserScores(page = page.toInt())
          } else {
              throw e
          }
      }
      emit(response)
  }


    fun removeApiKey(apiKey: String):Flow <DeleteApiKeyResponse> = flow {
        val keyHash = sha256Hex(apiKey)
        val response = try {
            scorefetcherService.deleteApiKey(
                keyHash = keyHash
            )
        } catch (e: HttpException) {
            throw e
        }
        emit(response)
    }


    fun getMaiTeaPlayerDetails(): Flow<MaiteaPlayerDetailsResponse?> = flow {
        // Check if cached data is still valid
        playerDetailsCache.get()?.let {
            emit(it)
            return@flow
        }

        // If not, make API call
        val response = maiteaProfileService.getPlayerDetails()
        response.let {
            // Update cache
            playerDetailsCache.put(it)
        }
        emit(response)
    }

    fun getProfiles(): Flow<Map<String, String>> = flow {
        emit(scorefetcherService.getProfiles())
    }

    fun get30BestCharts(hashKey: String? = null): Flow<List<PlayerBest30Response>> = flow {
        try {
            val key = hashKey ?: sha256Hex(apiKeyManager.getApiKey("maimai")) ?: ""
            if (key.isBlank()) {
                emit(emptyList())
                return@flow
            }
            val response = scorefetcherService.get30BestCharts(key)
            emit(response)
        } catch (e: Exception) {
            Log.e("MaimaiBest30Charts", "Error fetching best 30 charts: ${e.message}")
            emit(emptyList())
        }
    }

    fun getChartHistory(songName: String, difficulty: String? = null): Flow<List<ChartHistoryResponse>> = flow {
        try {
            val key = sha256Hex(apiKeyManager.getApiKey("maimai")) ?: ""
            var realDiff: String? = null;
            if (key.isBlank()) {
                emit(emptyList())
                return@flow
            }

            realDiff = null
            if (!difficulty.isNullOrBlank()) {
                realDiff = when (difficulty.trim().lowercase()) {
                    "remaster" -> "Re:Master"
                    "utage" -> "宴"
                    else -> difficulty
                }
            }

                val response = scorefetcherService.getChartHistory(key, songName, realDiff)
            emit(response)
        } catch (e: Exception) {
            Log.e("MaiteaRepository", "Error fetching chart history: ${e.message}")
            emit(emptyList())
        }
    }

    fun getBestPerPlayer(songName: String, difficulty: String? = null): Flow<List<BestPerPlayerResponse>> = flow {
        try {
            val response = scorefetcherService.getBestPerPlayer(songName, difficulty)

            emit(response)
        } catch (e: Exception) {
            Log.e("MaiteaRepository", "Error fetching best-per-player: ${e.message}")
            emit(emptyList())
        }
    }

    fun getPlayById(id: Int, keyHash: String): Flow<MaiteaApiData?> = flow {
        try {
            val response = scorefetcherService.getPlayById(id, keyHash)
            emit(response)
        } catch (e: Exception) {
            Log.e("MaiteaRepository", "Error fetching play by ID: ${e.message}")
            emit(null)
        }
    }

    fun searchCharts(query: String, keyHash: String? = null): Flow<List<BestPerPlayerResponse>> = flow {
        try {
            val key = keyHash ?: sha256Hex(apiKeyManager.getApiKey("maimai")) ?: ""
            val response = scorefetcherService.searchCharts(query, key)
            emit(response)
        } catch (e: Exception) {
            Log.e("MaiteaRepository", "Error searching charts: ${e.message}")
            emit(emptyList())
        }
    }

    fun getMostPlayed(
        limit: Int? = 30,
        period: String? = "month",
        date: String? = null
    ): Flow<List<org.arcade.atomcity.model.maitea.MaimaiMostPlayedEntry>> = flow {
        try {
            val response = when (period?.lowercase()) {
                "day" -> scorefetcherService.getMostPlayed(limit = limit, day = date)
                "week" -> scorefetcherService.getMostPlayed(limit = limit, week = date)
                "month" -> scorefetcherService.getMostPlayed(limit = limit, month = date)
                else -> scorefetcherService.getMostPlayed(limit = limit, period = period, date = date)
            }
            emit(response)
        } catch (e: Exception) {
            Log.e("MaiteaRepository", "Error fetching most played: ${e.message}")
            emit(emptyList())
        }
    }

    fun getMostPlayedByHash(
        keyHash: String? = null,
        limit: Int? = 30,
        period: String? = "month",
        date: String? = null
    ): Flow<List<org.arcade.atomcity.model.maitea.MaimaiMostPlayedEntry>> = flow {
        try {
            val key = keyHash ?: sha256Hex(apiKeyManager.getApiKey("maimai")) ?: ""
            if (key.isBlank()) {
                emit(emptyList())
                return@flow
            }
            val response = when (period?.lowercase()) {
                "day" -> scorefetcherService.getMostPlayedByHash(keyHash = key, limit = limit, day = date)
                "week" -> scorefetcherService.getMostPlayedByHash(keyHash = key, limit = limit, week = date)
                "month" -> scorefetcherService.getMostPlayedByHash(keyHash = key, limit = limit, month = date)
                else -> scorefetcherService.getMostPlayedByHash(keyHash = key, limit = limit, period = period, date = date)
            }
            emit(response)
        } catch (e: Exception) {
            Log.e("MaiteaRepository", "Error fetching personal most played: ${e.message}")
            emit(emptyList())
        }
    }


    fun sha256Hex(text: String?): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(text?.toByteArray(Charsets.UTF_8) ?: byteArrayOf())
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            val v = b.toInt() and 0xff
            if (v < 16) sb.append('0')
            sb.append(v.toString(16))
        }
        return sb.toString()
    }

}

data class ImportWorkerStatus(
    val isActive: Boolean,
    val state: String,
    val progress: Int,
    val message: String?
)
