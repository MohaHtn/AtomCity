/**
 * Get Scorefetcher Jacket Use Case
 *
 * Encapsulates business logic for finding song jacket image URLs.
 */
package org.arcade.atomcity.domain.usecase

import org.arcade.atomcity.domain.repository.IScorefetcherRepository

/**
 * Use case responsible for retrieving jacket image URLs for songs.
 * 
 * Adheres to the Single Responsibility Principle (SRP) by isolating
 * the jacket image lookup logic from other use cases.
 *
 * @property repository The [IScorefetcherRepository] data source.
 */
class GetScorefetcherJacketUseCase(private val repository: IScorefetcherRepository) {
    /**
     * Finds the jacket image URL associated with a specific song name.
     *
     * @param songName The name of the song to search for.
     * @return The URL string of the jacket image, or null if not found.
     */
    fun findJacketUrlBySongName(songName: String?): String? = repository.findJacketUrlBySongName(songName)
}
