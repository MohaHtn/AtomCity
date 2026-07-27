
package org.arcade.atomcity.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.arcade.atomcity.data.cache.DataCache
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse
import org.arcade.atomcity.network.DeleteApiKeyResponse
import org.arcade.atomcity.network.MaiteaProfileService
import org.arcade.atomcity.network.ScorefetcherService
import org.arcade.atomcity.network.ApiKeyRequest
import org.arcade.atomcity.utils.ApiKeyManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.arcade.atomcity.model.maitea.ChartHistoryResponse
import org.arcade.atomcity.model.maitea.playerBest30Response.PlayerBest30Response
import org.arcade.atomcity.worker.MaimaiImportWorker
import retrofit2.HttpException
import java.security.MessageDigest

class MaiteaRepository(
    private val scorefetcherService: ScorefetcherService,
    private val apiKeyManager: ApiKeyManager,
    private val maiteaProfileService: MaiteaProfileService,
    private val workManager: WorkManager
) {
    // Cache for paginated data
    private val playsCache = mutableMapOf<Int, DataCache<MaiteaPlaysResponse>>()

    // Cache for player details
    private val playerDetailsCache = DataCache<MaiteaPlayerDetailsResponse>()

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
            // Register the API key and emit null (no data)
            scorefetcherService.addApiKey(
                ApiKeyRequest(key = apiKey, description = "MaiTea Key")
            )
            startImportWorker(apiKey)
            emit(null)
        }
    }

    fun addApiKey(apikey: String) = flow {
        scorefetcherService.checkApiKey(apikey).let {
            if (it.isKeyProvidedInDatabase) {
                scorefetcherService.addApiKey(
                    ApiKeyRequest(key = apikey, description = "MaiTea Key")
                )

                startImportWorker(apikey)

                return@flow
            }
        }

        emit(true)
    }

    private fun startImportWorker(apiKey: String) {
        val workRequest = OneTimeWorkRequestBuilder<MaimaiImportWorker>()
            .setInputData(MaimaiImportWorker.createInputData(apiKey))
            .build()
        workManager.enqueue(workRequest)
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
        val response = try {
            scorefetcherService.deleteApiKey(
                apikey = apiKey
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

    fun getProfiles(): Flow<Map<String, List<String>>> = flow {
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

    fun getBestPerPlayer(songName: String): Flow<List<org.arcade.atomcity.model.maitea.BestPerPlayerResponse>> = flow {
        try {
            val key = sha256Hex(apiKeyManager.getApiKey("maimai")) ?: ""
            if (key.isBlank()) {
                emit(emptyList())
                return@flow
            }
            val response = scorefetcherService.getBestPerPlayer(key, songName)
            emit(response)
        } catch (e: Exception) {
            Log.e("MaiteaRepository", "Error fetching best-per-player: ${e.message}")
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
