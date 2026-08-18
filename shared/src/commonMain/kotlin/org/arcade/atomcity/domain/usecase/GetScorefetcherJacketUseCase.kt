package org.arcade.atomcity.domain.usecase

import org.arcade.atomcity.domain.repository.IScorefetcherRepository

class GetScorefetcherJacketUseCase(private val repository: IScorefetcherRepository) {
    fun findJacketUrlBySongName(songName: String?): String? = repository.findJacketUrlBySongName(songName)
}
