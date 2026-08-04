package org.arcade.atomcity.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import org.arcade.atomcity.model.maitea.ChartHistoryResponse
import org.arcade.atomcity.model.maitea.playerBest30Response.PlayerBest30Response
import org.arcade.atomcity.model.maitea.BestPerPlayerResponse
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaApiData

@Serializable
data class ApiKeyRequest(
    val key: String,
    val description: String
)

@Serializable
data class ApiKeyCheckResponse(
    val isKeyProvidedInDatabase: Boolean,
)

@Serializable
data class AddApiKeyResponse(
    val message: String,
    val keyHash: String,
    val importStream: String
)

@Serializable
data class DeleteApiKeyResponse(
    val message: String,
)

class ScorefetcherClient(
    private val client: HttpClient, 
    private val apiKey: String,
    private val baseUrl: String = "https://scorefetcher.mohahtn.xyz/"
) {
    private fun io.ktor.client.request.HttpRequestBuilder.addApiKey() {
        header("X-API-KEY", apiKey)
    }

    suspend fun addApiKey(request: ApiKeyRequest): AddApiKeyResponse =
        client.post("${baseUrl}apikeys") {
            addApiKey()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun getScores(token: String, pageNumber: String): MaiteaPlaysResponse =
        client.get("${baseUrl}scores") {
            addApiKey()
            header("Authorization", token)
            parameter("pageNumber", pageNumber)
        }.body()

    suspend fun checkApiKey(key: String): ApiKeyCheckResponse =
        client.get("${baseUrl}apikeys/check") {
            addApiKey()
            parameter("key", key)
        }.body()

    suspend fun getProfiles(): Map<String, String> =
        client.get("${baseUrl}apikeys/profiles") {
            addApiKey()
        }.body()

    suspend fun deleteApiKey(keyHash: String): DeleteApiKeyResponse =
        client.delete("${baseUrl}apikeys/$keyHash") {
            addApiKey()
        }.body()

    suspend fun get30BestCharts(hashKey: String): List<PlayerBest30Response> =
        client.get("${baseUrl}scores/top") {
            addApiKey()
            parameter("keyHash", hashKey)
        }.body()

    suspend fun getChartHistory(
        hashKey: String,
        songName: String,
        difficulty: String? = null
    ): List<ChartHistoryResponse> =
        client.get("${baseUrl}scores/history") {
            addApiKey()
            parameter("keyHash", hashKey)
            parameter("songName", songName)
            parameter("difficulty", difficulty)
        }.body()

    suspend fun getBestPerPlayer(
        songName: String,
        difficulty: String? = null
    ): List<BestPerPlayerResponse> =
        client.get("${baseUrl}scores/best-per-player") {
            addApiKey()
            parameter("songName", songName)
            parameter("difficulty", difficulty)
        }.body()

    suspend fun getPlayById(id: Int, keyHash: String): MaiteaApiData =
        client.get("${baseUrl}scores/$id") {
            addApiKey()
            parameter("keyHash", keyHash)
        }.body()

    suspend fun searchCharts(query: String, keyHash: String? = null): List<BestPerPlayerResponse> =
        client.get("${baseUrl}scores/search") {
            addApiKey()
            parameter("query", query)
            parameter("keyHash", keyHash)
        }.body()
}
