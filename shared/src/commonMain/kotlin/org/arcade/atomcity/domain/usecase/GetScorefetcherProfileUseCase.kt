package org.arcade.atomcity.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.domain.repository.IScorefetcherRepository
import org.arcade.atomcity.data.remote.model.scorefetcher.playerDetailsResponse.ScorefetcherPlayerDetailsResponse

class GetScorefetcherProfileUseCase(private val repository: IScorefetcherRepository) {
    fun getScorefetcherPlayerDetails(): Flow<ScorefetcherPlayerDetailsResponse?> = repository.getScorefetcherPlayerDetails()

    fun getProfiles(): Flow<Map<String, String?>> = repository.getProfiles()

    fun getRatings(): Flow<Map<String, Int?>> = repository.getRatings()
}
