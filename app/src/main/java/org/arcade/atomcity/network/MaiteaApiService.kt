package org.arcade.atomcity.network

import org.arcade.atomcity.model.maitea.MaiTeaResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MaiteaApiService {
    @GET("plays")
    suspend fun getAllUserScores(
        @Query("page") page: Int
    ): MaiTeaResponse
}