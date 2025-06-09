package org.arcade.atomcity.data

import org.arcade.atomcity.data.cache.DataCache
import org.arcade.atomcity.domain.usecase.GetTaikoServerDataUseCase
import org.arcade.atomcity.model.taikoserver.musicDetails.TaikoServerMusicDetails
import org.arcade.atomcity.model.taikoserver.songHistory.TaikoServerPlayHistoryResponse

class TaikoServerRepository(private val getTaikoServerDataUseCase: GetTaikoServerDataUseCase) {

    // Cache for pagnitated music details
    private val taikoServerCache = mutableMapOf<Int, DataCache<TaikoServerPlayHistoryResponse>>()

    // Cache for music details
    private val musicDetailsCache = DataCache<TaikoServerMusicDetails>()

    // TODO : for now, the taiko server music details are not paginated,
    //  but if they become paginated in the future, we can implement caching for that as well.
    suspend fun getTaikoServerPaginatedData(page: Int): TaikoServerPlayHistoryResponse? {
        // Get or create cache for this page
        val pageCache = taikoServerCache.getOrPut(1) { DataCache() }

        // Check if data is in cache
        pageCache.get()?.let {
            return it
        }

        // If not, make API call
        var response: TaikoServerPlayHistoryResponse? = null
        getTaikoServerDataUseCase.getPlayHistoryFlow(1.toString()).collect { data ->
            data?.let {
                // Store data in cache
                pageCache.put(it)
                response = it
            }
        }
        return response
    }
}