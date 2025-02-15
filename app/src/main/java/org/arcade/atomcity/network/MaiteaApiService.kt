package org.arcade.atomcity.network

import org.arcade.atomcity.model.maitea.MaiTeaResponse
import retrofit2.http.GET

interface MaiteaApiService {
    @GET("plays/all")
    suspend fun getAllUserScores(): MaiTeaResponse
}