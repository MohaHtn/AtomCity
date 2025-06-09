
package org.arcade.atomcity.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.arcade.atomcity.data.cache.DataCache
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse
import org.arcade.atomcity.network.MaiteaService

class MaiteaRepository(private val maiteaService: MaiteaService) {

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
        val response = maiteaService.getAllUserScores(page)
        response?.let {
            // Store data in cache
            pageCache.put(it)
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
        val response = maiteaService.getPlayerDetails()
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
