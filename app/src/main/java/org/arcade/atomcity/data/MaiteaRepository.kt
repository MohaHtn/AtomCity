
package org.arcade.atomcity.data

import android.util.Log
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.arcade.atomcity.data.cache.DataCache
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse
import org.arcade.atomcity.network.DeleteApiKeyResponse
import org.arcade.atomcity.network.MaiteaProfileService
import org.arcade.atomcity.network.MaiteaService
import org.arcade.atomcity.utils.ApiKeyManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.arcade.atomcity.model.maitea.playerBest30Response.PlayerBest30Response
import org.arcade.atomcity.worker.MaimaiImportWorker
import retrofit2.HttpException

class MaiteaRepository(
    private val maiteaService: MaiteaService,
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

        val keyCheck = maiteaService.checkApiKey(apiKey)
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
            maiteaService.addApiKey(
                key = apiKey, description = "MIKU MIKU BEAM!"
            )
            startImportWorker(apiKey)
            emit(null)
        }
    }

    fun addApiKey(apikey: String) = flow {
        maiteaService.checkApiKey(apikey).let {
            if (it.isKeyProvidedInDatabase) {
                maiteaService.addApiKey(
                    key = apikey,
                    description = "maimai API key"
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
          maiteaService.getScores(token = token, pageNumber = page)
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
            maiteaService.deleteApiKey(
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
        emit(maiteaService.getProfiles())
    }

    //TODO: check why it returns as an Any?.
    fun get30BestCharts(hashKey: MutableState<String>): Flow<PlayerBest30Response> = flow {
        val response  = try {
                maiteaService.get30BestCharts(hashKey)
        }
        catch (e: Exception){
            Log.e("MaimaiBest30Charts", "Error fetching best 30 charts: ${e.message}")
        }
        emit(response as PlayerBest30Response)
    }

}
