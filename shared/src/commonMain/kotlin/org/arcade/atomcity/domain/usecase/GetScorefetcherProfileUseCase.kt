package org.arcade.atomcity.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.data.ScorefetcherRepository
import org.arcade.atomcity.model.scorefetcher.playerDetailsResponse.ScorefetcherPlayerDetailsResponse

class GetScorefetcherProfileUseCase(private val repository: ScorefetcherRepository) {
    fun getScorefetcherPlayerDetails(): Flow<ScorefetcherPlayerDetailsResponse?> = repository.getScorefetcherPlayerDetails()

    fun getProfiles(): Flow<Map<String, String?>> = repository.getProfiles()

    fun getRatings(): Flow<Map<String, Int?>> = repository.getRatings()
}
