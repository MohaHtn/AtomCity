
package org.arcade.atomcity.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.arcade.atomcity.data.cache.DataCache
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse
import org.arcade.atomcity.network.ApiCheckRequest
import org.arcade.atomcity.network.ApiKeyRequest
import org.arcade.atomcity.network.MaiteaProfileService
import org.arcade.atomcity.network.MaiteaService
import org.arcade.atomcity.utils.ApiKeyManager

class MaiteaRepository(
    private val maiteaService: MaiteaService,
    private val apiKeyManager: ApiKeyManager,
    private val maiteaProfileService: MaiteaProfileService
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
        if (keyCheck.exists) {
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
            emit(null)
        }
    }

    fun addApiKey(apikey: String) = flow {
        maiteaService.checkApiKey(apikey).let {
            if (it.exists) {
                maiteaService.addApiKey(
                    key = apikey,
                    description = "MIKU MIKU BEAM!"
                )
                return@flow
            }
        }

        emit(true)
    }

  fun getScores(token: String, page: String): Flow<MaiteaPlaysResponse?> = flow {
      var response: MaiteaPlaysResponse?
        try {
            response = maiteaService.getScores(token = token, pageNumber = page)
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 404) {
                response = maiteaProfileService.getAllUserScores(page = page.toInt())
            } else {
                throw e
            }
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

    // Method to clear cache if needed
    fun clearCache() {
        playsCache.values.forEach { it.clear() }
        playsCache.clear()
        playerDetailsCache.clear()
    }
}
