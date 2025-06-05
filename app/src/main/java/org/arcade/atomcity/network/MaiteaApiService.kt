package org.arcade.atomcity.network

import org.arcade.atomcity.model.maitea.PlaysResponse.MaiteaPlaysResponse
import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MaiteaApiService {
    @GET("plays")
    suspend fun getAllUserScores(
        @Query("page") page: Int
    ): MaiteaPlaysResponse

    @GET("profiles")
    suspend fun getPlayerDetails(
    ): MaiteaPlayerDetailsResponse
}