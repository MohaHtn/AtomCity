package org.arcade.atomcity.network

import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MaiteaProfileService {

    @GET("profiles")
    suspend fun getPlayerDetails(
    ): MaiteaPlayerDetailsResponse

    @GET("plays")
    suspend fun getAllUserScores(
        @Query("page") page: Int
    ): MaiteaPlaysResponse
}