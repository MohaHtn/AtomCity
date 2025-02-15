package org.arcade.atomcity.domain.usecase

    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.withContext
    import org.arcade.atomcity.model.maitea.MaiteaApiResponse
    import org.arcade.atomcity.network.MaiteaApiService

    class GetMaiteatDataUseCase(private val maiteaApiService: MaiteaApiService) {

        suspend fun execute(): MaiteaApiResponse? {
            return withContext(Dispatchers.IO) {
                val response = maiteaApiService.getAllUserScores()
                response
            }
        }
    }