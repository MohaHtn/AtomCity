package org.arcade.atomcity.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.domain.repository.IScorefetcherRepository
import org.arcade.atomcity.data.remote.model.scorefetcher.BestPerPlayerResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.ChartHistoryResponse
import org.arcade.atomcity.data.remote.model.scorefetcher.MaimaiMostPlayedEntry

class GetScorefetcherAnalyticsUseCase(private val repository: IScorefetcherRepository) {
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
