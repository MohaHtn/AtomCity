package org.arcade.atomcity.network

import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

data class ApiKeyRequest(
    val apikey: String,
    val description: String
)

data class ApiKeyCheckResponse(
    val exists: Boolean,
)

data class ApiCheckRequest(
    val key: String,
)

interface MaiteaService {
    @FormUrlEncoded
    @POST("apikeys")
    suspend fun addApiKey(
        @Field("key") key: String,
        @Field("description") description: String
    )

    @FormUrlEncoded
    @POST("scores")
    suspend fun getScores(
        @Field("token") token: String,
        @Field("pageNumber") pageNumber: String
    ): MaiteaPlaysResponse

    @FormUrlEncoded
    @POST("apikeys/check")
    suspend fun checkApiKey(
        @Field("key") key: String
    ): ApiKeyCheckResponse

    @GET("apikeys/profiles")
    suspend fun getProfiles(): Map<String, List<String>>
}