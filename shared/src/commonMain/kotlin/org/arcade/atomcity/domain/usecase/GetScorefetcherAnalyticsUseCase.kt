package org.arcade.atomcity.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.data.ScorefetcherRepository
import org.arcade.atomcity.model.scorefetcher.BestPerPlayerResponse
import org.arcade.atomcity.model.scorefetcher.ChartHistoryResponse
import org.arcade.atomcity.model.scorefetcher.MaimaiMostPlayedEntry

class GetScorefetcherAnalyticsUseCase(private val repository: ScorefetcherRepository) {
    fun getChartHistory(songName: String, difficulty: String?): Flow<List<ChartHistoryResponse>> = repository.getChartHistory(songName, difficulty)

    fun getBestPerPlayer(songName: String, difficulty: String?): Flow<List<BestPerPlayerResponse>> = repository.getBestPerPlayer(songName, difficulty)

    fun getMostPlayed(
        limit: Int? = 30,
        period: String? = "month",
        date: String? = null,
        groupByHashkey: Boolean = false
    ): Flow<List<MaimaiMostPlayedEntry>> = repository.getMostPlayed(limit, period, date, groupByHashkey)

    fun getMostPlayedByHash(
        keyHash: String? = null,
        limit: Int? = 30,
        period: String? = "month",
        date: String? = null,
        groupByHashkey: Boolean = false
    ): Flow<List<MaimaiMostPlayedEntry>> = repository.getMostPlayedByHash(keyHash, limit, period, date, groupByHashkey)
}
