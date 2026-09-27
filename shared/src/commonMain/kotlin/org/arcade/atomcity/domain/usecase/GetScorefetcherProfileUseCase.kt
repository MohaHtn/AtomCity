package org.arcade.atomcity.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.arcade.atomcity.domain.repository.IScorefetcherRepository
import org.arcade.atomcity.data.remote.model.scorefetcher.playerDetailsResponse.ScorefetcherPlayerDetailsResponse

/**
 * Use case responsible for retrieving player profile details, ratings, and associated profiles.
 * 
 * Adheres to the Single Responsibility Principle (SRP) by encapsulating profile-related operations.
 *
 * @property repository The [IScorefetcherRepository] data source.
 */
class GetScorefetcherProfileUseCase(private val repository: IScorefetcherRepository) {
    /**
     * Retrieves the details of the player.
     *
     * @return A [Flow] emitting the [ScorefetcherPlayerDetailsResponse].
     */
    fun getScorefetcherPlayerDetails(): Flow<ScorefetcherPlayerDetailsResponse?> = repository.getScorefetcherPlayerDetails()

    /**
     * Retrieves the mapping of player profiles.
     *
     * @return A [Flow] emitting a map of user key hashes to their display names.
     */
    fun getProfiles(): Flow<Map<String, String?>> = repository.getProfiles()

    /**
     * Retrieves the player ratings.
     *
     * @return A [Flow] emitting a map of user key hashes to their rating values.
     */
    fun getRatings(): Flow<Map<String, Int?>> = repository.getRatings()
}
