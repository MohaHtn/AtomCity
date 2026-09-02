package org.arcade.atomcity.domain.repository

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.domain.model.ImportWorkerStatus
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherPlaysResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.playerDetailsResponse.ScorefetcherPlayerDetailsResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.playerBest30Response.PlayerBest30Response
import org.arcade.atomcity.data.remote.model.scorefetcher.ChartHistoryResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.BestPerPlayerResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.playsResponse.ScorefetcherApiData
import org.arcade.atomcity.data.remote.model.scorefetcher.MaimaiMostPlayedEntry
import org.arcade.atomcity.data.remote.DeleteApiKeyResponse

interface IScorefetcherRepository {
    fun findJacketUrlBySongName(songName: String?): String?
    fun observeImportWorkerStatus(): Flow<ImportWorkerStatus?>
    suspend fun refreshImportWorkerStatus(): ImportWorkerStatus
    fun setImportWorkerActive(active: Boolean)
    fun isImportWorkerActive(): Flow<Boolean>
    fun clearScorefetcherPaginatedCache()
    suspend fun startScorefetcherImport(): Boolean
    fun getScorefetcherPaginatedData(page: Int): Flow<ScorefetcherPlaysResponse?>
    fun getScorefetcherPlayerDetails(): Flow<ScorefetcherPlayerDetailsResponse?>
    fun getProfiles(): Flow<Map<String, String?>>
    fun getRatings(): Flow<Map<String, Int?>>
    fun get30BestCharts(hashKey: String? = null): Flow<List<PlayerBest30Response>>
    fun getTopUtageScores(hashKey: String? = null): Flow<List<PlayerBest30Response>>
    fun getChartHistory(songName: String, difficulty: String?): Flow<List<ChartHistoryResponse>>
    fun getBestPerPlayer(songName: String, difficulty: String?): Flow<List<BestPerPlayerResponse>>
    fun getPlayById(id: Int, keyHash: String): Flow<ScorefetcherApiData?>
    fun searchCharts(query: String, keyHash: String? = null): Flow<List<BestPerPlayerResponse>>
    fun getMostPlayed(limit: Int? = 30, period: String? = "month", date: String? = null, groupByHashkey: Boolean = false): Flow<List<MaimaiMostPlayedEntry>>
    fun getMostPlayedByHash(keyHash: String? = null, limit: Int? = 30, period: String? = "month", date: String? = null, groupByHashkey: Boolean = false): Flow<List<MaimaiMostPlayedEntry>>
    suspend fun removeApiKey(apiKey: String): Flow<DeleteApiKeyResponse>
    suspend fun addTaikoUser(baid: Int): Boolean
    fun getTaikoUsers(): Flow<List<org.arcade.atomcity.data.remote.TaikoUser>>
}
