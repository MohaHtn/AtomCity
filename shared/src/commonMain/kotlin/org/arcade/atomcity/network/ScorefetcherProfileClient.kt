package org.arcade.atomcity.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.arcade.atomcity.model.scorefetcher.playerDetailsResponse.ScorefetcherPlayerDetailsResponse

class ScorefetcherProfileClient(private val client: HttpClient, private val baseUrl: String = "https://maitea.app/api/v1/") {
    suspend fun getPlayerDetails(token: String): ScorefetcherPlayerDetailsResponse =
        client.get("${baseUrl}profiles") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            header("Accept", ContentType.Application.Json)
        }.body()
}
