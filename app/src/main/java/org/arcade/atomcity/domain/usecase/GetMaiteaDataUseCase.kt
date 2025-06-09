package org.arcade.atomcity.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.arcade.atomcity.data.MaiteaRepository
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse

class GetMaiteaDataUseCase(private val maiTeaRepository: MaiteaRepository) {

    suspend fun execute(page: Int): MaiteaPlaysResponse? {
        return withContext(Dispatchers.IO) {
            val response = maiTeaRepository.getMaiTeaPaginatedData(page)
            response.first()
        }
    }

    suspend fun executePlayerDetails(): MaiteaPlayerDetailsResponse? {
        return withContext(Dispatchers.IO) {
            val response = maiTeaRepository.getMaiTeaPlayerDetails()
            response.first()
        }
    }
}