package org.arcade.atomcity.network

import org.arcade.atomcity.model.maitea.ChartHistoryResponse
import org.arcade.atomcity.model.maitea.playerBest30Response.PlayerBest30Response
import org.arcade.atomcity.model.maitea.BestPerPlayerResponse
import retrofit2.http.Path
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaApiData
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

data class ApiKeyRequest(
    val key: String,
    val description: String
)

data class ApiKeyCheckResponse(
    val isKeyProvidedInDatabase: Boolean,
)

data class AddApiKeyResponse(
    val message: String,
    val keyHash: String,
    val importStream: String
)

data class DeleteApiKeyResponse(
    val message: String,
)

interface ScorefetcherService {
    @POST("apikeys")
    suspend fun addApiKey(
        @Body request: ApiKeyRequest
    ): AddApiKeyResponse
    @GET("scores")
    suspend fun getScores(
        @Header("Authorization") token: String,
        @Query("pageNumber") pageNumber: String
    ): MaiteaPlaysResponse

    @GET("apikeys/check")
    suspend fun checkApiKey(
        @Query("key") key: String
    ): ApiKeyCheckResponse

    @GET("apikeys/profiles")
    suspend fun getProfiles(): Map<String, String>

    @DELETE("apikeys/{keyHash}")
    suspend fun deleteApiKey(
        @Path("keyHash") keyHash: String
    ): DeleteApiKeyResponse

    @GET("scores/top")
    suspend fun get30BestCharts(
        @Query("keyHash") hashKey: String
    ): List<PlayerBest30Response>

    @GET("scores/history")
    suspend fun getChartHistory(
        @Query("keyHash") hashKey: String,
            @Query("songName") songName: String,
            @Query("difficulty") difficulty: String? = null
        ): List<ChartHistoryResponse>

    @GET("scores/best-per-player")
    suspend fun getBestPerPlayer(
        @Query("songName") songName: String,
        @Query("difficulty") difficulty: String? = null
    ): List<BestPerPlayerResponse>

    @GET("scores/{id}")
    suspend fun getPlayById(
        @Path("id") id: Int,
        @Query("keyHash") keyHash: String
    ): MaiteaApiData

    @GET("scores/search")
    suspend fun searchCharts(
        @Query("query") query: String,
        @Query("keyHash") keyHash: String? = null
    ): List<BestPerPlayerResponse>

    @GET("scores/most-played")
    suspend fun getMostPlayed(
        @Query("limit") limit: Int? = null,
        @Query("date") date: String? = null,
        @Query("month") month: String? = null,
        @Query("week") week: String? = null,
        @Query("day") day: String? = null,
        @Query("period") period: String? = null
    ): List<org.arcade.atomcity.model.maitea.MaimaiMostPlayedEntry>

    @GET("scores/most-played/by-keyhash")
    suspend fun getMostPlayedByHash(
        @Query("keyHash") keyHash: String,
        @Query("limit") limit: Int? = null,
        @Query("date") date: String? = null,
        @Query("month") month: String? = null,
        @Query("week") week: String? = null,
        @Query("day") day: String? = null,
        @Query("period") period: String? = null
    ): List<org.arcade.atomcity.model.maitea.MaimaiMostPlayedEntry>
}