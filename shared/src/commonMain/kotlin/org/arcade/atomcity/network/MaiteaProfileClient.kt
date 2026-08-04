package org.arcade.atomcity.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse

class MaiteaProfileClient(private val client: HttpClient, private val baseUrl: String = "https://maitea.app/api/v1/") {
    suspend fun getPlayerDetails(): MaiteaPlayerDetailsResponse =
        client.get("${baseUrl}profiles").body()

    suspend fun getAllUserScores(page: Int): MaiteaPlaysResponse =
        client.get("${baseUrl}plays") {
            parameter("page", page)
        }.body()
}
