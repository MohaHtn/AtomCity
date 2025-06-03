package org.arcade.atomcity.domain.usecase

    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.withContext
    import org.arcade.atomcity.model.maitea.MaiTeaResponse
    import org.arcade.atomcity.network.MaiteaApiService

    class GetMaiTeaDataUseCase(private val maiteaApiService: MaiteaApiService) {

        suspend fun execute(page: Int): MaiTeaResponse? {
            return withContext(Dispatchers.IO) {
                val response = maiteaApiService.getAllUserScores(page)
                response
            }
        }
    }