package org.arcade.atomcity.domain.usecase

    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.withContext
    import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
    import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse
    import org.arcade.atomcity.network.MaiteaApiService

    class GetMaiTeaDataUseCase(private val maiteaApiService: MaiteaApiService) {

        suspend fun execute(page: Int): MaiteaPlaysResponse? {
            return withContext(Dispatchers.IO) {
                val response = maiteaApiService.getAllUserScores(page)
                response
            }
        }

        suspend fun executePlayerDetails(): MaiteaPlayerDetailsResponse? {
            return withContext(Dispatchers.IO) {
                val response = maiteaApiService.getPlayerDetails()
                response
            }
        }
    }