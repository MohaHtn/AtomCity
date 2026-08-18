package org.arcade.atomcity.domain.usecase

import org.arcade.atomcity.data.ScorefetcherRepository

class GetScorefetcherJacketUseCase(private val repository: ScorefetcherRepository) {
    fun findJacketUrlBySongName(songName: String?): String? = repository.findJacketUrlBySongName(songName)
}
