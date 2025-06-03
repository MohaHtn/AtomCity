package org.arcade.atomcity.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.arcade.atomcity.domain.usecase.GetMaiTeaDataUseCase
import org.arcade.atomcity.model.maitea.MaiTeaResponse

class MaiTeaRepository(private val getMaiTeaDataUseCase: GetMaiTeaDataUseCase) {

    fun getMaiTeaPaginatedData(page: Int): Flow<MaiTeaResponse?> = flow {
        emit(getMaiTeaDataUseCase.execute(page))
    }
}