package org.arcade.atomcity.network

import org.arcade.atomcity.model.maitea.MaiteaApiResponse
import retrofit2.http.GET

interface MaiteaApiService {
    @GET("api/v1/plays/all/")
    suspend fun getAllUserScores(): MaiteaApiResponse
}