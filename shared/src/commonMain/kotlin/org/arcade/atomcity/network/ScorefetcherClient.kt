package org.arcade.atomcity.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import org.arcade.atomcity.model.maitea.ChartHistoryResponse
import org.arcade.atomcity.model.maitea.playerBest30Response.PlayerBest30Response
import org.arcade.atomcity.model.maitea.BestPerPlayerResponse
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaApiData
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
import org.arcade.atomcity.model.maitea.MaimaiMostPlayedEntry

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

    suspend fun getScores(token: String, pageNumber: String): MaiteaPlaysResponse {
        val response: HttpResponse = client.get("${baseUrl}scores") {
            addApiKey()
            header("Authorization", token)
            header("Accept", "application/json")
            parameter("pageNumber", pageNumber)
        }
        return if (response.status == HttpStatusCode.NotFound) {
            MaiteaPlaysResponse(emptyList())
        } else {
            response.body()
        }
    }

    suspend fun checkApiKey(key: String): ApiKeyCheckResponse =
        client.get("${baseUrl}apikeys/check") {
            addApiKey()
            parameter("key", key)
        }.body()

    suspend fun getProfiles(): Map<String, String> =
        client.get("${baseUrl}apikeys/profiles") {
            addApiKey()
            header("Accept", "application/json")
        }.body()

    suspend fun getRatings(): Map<String, Int> =
        client.get("${baseUrl}apikeys/ratings") {
            addApiKey()
            header("Accept", "application/json")
        }.body()

    suspend fun deleteApiKey(keyHash: String): DeleteApiKeyResponse =
        client.delete("${baseUrl}apikeys/$keyHash") {
            addApiKey()
        }.body()

    suspend fun get30BestCharts(hashKey: String): List<PlayerBest30Response> {
        val response: HttpResponse = client.get("${baseUrl}scores/top") {
            addApiKey()
            header("Accept", "application/json")
            parameter("keyHash", hashKey)
        }
        return if (response.status == HttpStatusCode.NotFound) {
            emptyList()
        } else {
            response.body()
        }
    }

    suspend fun getChartHistory(
        hashKey: String,
        songName: String,
        difficulty: String? = null
    ): List<ChartHistoryResponse> {
        val response: HttpResponse = client.get("${baseUrl}scores/history") {
            addApiKey()
            header("Accept", "application/json")
            parameter("keyHash", hashKey)
            parameter("songName", songName)
            parameter("difficulty", difficulty)
        }
        return if (response.status == HttpStatusCode.NotFound) {
            emptyList()
        } else {
            response.body()
        }
    }

    suspend fun getBestPerPlayer(
        songName: String,
        difficulty: String? = null
    ): List<BestPerPlayerResponse> {
        val response: HttpResponse = client.get("${baseUrl}scores/best-per-player") {
            addApiKey()
            header("Accept", "application/json")
            parameter("songName", songName)
            parameter("difficulty", difficulty)
        }
        return if (response.status == HttpStatusCode.NotFound) {
            emptyList()
        } else {
            response.body()
        }
    }

    suspend fun getPlayById(id: Int, keyHash: String): MaiteaApiData {
        val response: HttpResponse = client.get("${baseUrl}scores/$id") {
            addApiKey()
            header("Accept", "application/json")
            parameter("keyHash", keyHash)
        }
        return if (response.status == HttpStatusCode.NotFound) {
            throw Exception("Score $id not found")
        } else {
            response.body()
        }
    }

    suspend fun searchCharts(query: String, keyHash: String? = null): List<BestPerPlayerResponse> {
        val response: HttpResponse = client.get("${baseUrl}scores/search") {
            addApiKey()
            parameter("query", query)
            parameter("keyHash", keyHash)
        }
        return if (response.status == HttpStatusCode.NotFound) {
            emptyList()
        } else {
            response.body()
        }
    }

    suspend fun getMostPlayed(
        limit: Int? = 30,
        period: String? = null,
        date: String? = null,
        day: String? = null,
        week: String? = null,
        month: String? = null,
        alltime: String? = null,
        groupByHashkey: Boolean = false
    ): List<MaimaiMostPlayedEntry> {
        val response: HttpResponse = client.get("${baseUrl}scores/most-played") {
            addApiKey()
            parameter("limit", limit)
            parameter("period", period)
            parameter("date", date)
            parameter("day", day)
            parameter("week", week)
            parameter("month", month)
            parameter("alltime", alltime)
            parameter("groupByHashkey", groupByHashkey)
        }
        return if (response.status == HttpStatusCode.NotFound) {
            emptyList()
        } else if (groupByHashkey) {
            val grouped: Map<String, List<MaimaiMostPlayedEntry>> = response.body()
            processGroupedEntries(grouped, limit ?: 30)
        } else {
            response.body()
        }
    }

    suspend fun getMostPlayedByHash(
        keyHash: String,
        limit: Int? = 30,
        period: String? = null,
        date: String? = null,
        day: String? = null,
        week: String? = null,
        month: String? = null,
        alltime: String? = null,
        groupByHashkey: Boolean = false
    ): List<MaimaiMostPlayedEntry> {
        val response: HttpResponse = client.get("${baseUrl}scores/most-played/by-keyhash") {
            addApiKey()
            parameter("keyHash", keyHash)
            parameter("limit", limit)
            parameter("period", period)
            parameter("date", date)
            parameter("day", day)
            parameter("week", week)
            parameter("month", month)
            parameter("alltime", alltime)
            parameter("groupByHashkey", groupByHashkey)
        }
        return if (response.status == HttpStatusCode.NotFound) {
            emptyList()
        } else if (groupByHashkey) {
            val grouped: Map<String, List<MaimaiMostPlayedEntry>> = response.body()
            processGroupedEntries(grouped, limit ?: 30)
        } else {
            response.body()
        }
    }

    private fun processGroupedEntries(
        grouped: Map<String, List<MaimaiMostPlayedEntry>>,
        limit: Int
    ): List<MaimaiMostPlayedEntry> {
        val chartMap = mutableMapOf<String, MaimaiMostPlayedEntry>()

        grouped.forEach { (hash, entries) ->
            entries.forEach { entry ->
                val chartKey = "${entry.songName}_${entry.difficulty}"
                val existing = chartMap[chartKey]
                
                if (existing == null) {
                    chartMap[chartKey] = entry.copy(
                        userPlayCounts = mutableMapOf(hash to entry.playCount)
                    )
                } else {
                    val updatedCounts = (existing.userPlayCounts?.toMutableMap() ?: mutableMapOf()).apply {
                        put(hash, entry.playCount)
                    }
                    chartMap[chartKey] = existing.copy(
                        playCount = existing.playCount + entry.playCount,
                        userPlayCounts = updatedCounts,
                        songNameEn = entry.songNameEn,
                        songNameJp = entry.songNameJp,
                        playPercentage = (existing.playPercentage ?: 0.0) + (entry.playPercentage ?: 0.0)
                    )
                }
            }
        }

        return chartMap.values
            .sortedByDescending { it.playCount }
            .take(limit)
    }
}
