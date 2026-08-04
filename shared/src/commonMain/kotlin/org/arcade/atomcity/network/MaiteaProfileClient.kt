package org.arcade.atomcity.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse

class MaiteaProfileClient(private val client: HttpClient, private val baseUrl: String = "https://maitea.app/api/v1/") {
    suspend fun getPlayerDetails(token: String): MaiteaPlayerDetailsResponse =
        client.get("${baseUrl}profiles") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            header("Accept", ContentType.Application.Json)
        }.body()

    suspend fun getAllUserScores(token: String, page: Int): MaiteaPlaysResponse =
        client.get("${baseUrl}plays") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            header("Accept", ContentType.Application.Json)
            parameter("page", page)
        }.body()
}
