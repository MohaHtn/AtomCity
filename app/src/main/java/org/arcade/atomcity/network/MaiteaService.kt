package org.arcade.atomcity.network

import retrofit2.http.Path
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST

data class ApiKeyRequest(
    val apikey: String,
    val description: String
)

data class ApiKeyCheckResponse(
    val isKeyProvidedInDatabase: Boolean,
)

data class DeleteApiKeyResponse(
    val message: String,
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

    @DELETE("apikeys/{apikey}")
    suspend fun deleteApiKey(
        @Path("apikey") apikey: String
    ): DeleteApiKeyResponse
}