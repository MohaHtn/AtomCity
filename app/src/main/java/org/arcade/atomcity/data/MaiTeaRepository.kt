package org.arcade.atomcity.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.arcade.atomcity.domain.usecase.GetMaiTeaDataUseCase
import org.arcade.atomcity.model.maitea.PlaysResponse.MaiteaPlaysResponse

class MaiTeaRepository(private val getMaiTeaDataUseCase: GetMaiTeaDataUseCase) {

    fun getMaiTeaPaginatedData(page: Int): Flow<MaiteaPlaysResponse?> = flow {
        emit(getMaiTeaDataUseCase.execute(page))
    }

    fun getMaiTeaPlayerDetails(): Flow<MaiteaPlayerDetailsResponse?> = flow {
        emit(getMaiTeaDataUseCase.executePlayerDetails())
    }
}