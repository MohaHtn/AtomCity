package org.arcade.atomcity.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.arcade.atomcity.worker.MaimaiImportEvent
import org.arcade.atomcity.utils.PlatformUtils

class ImportService(
    private val client: HttpClient,
    private val scorefetcherApiKey: String,
    private val baseUrl: String = "https://scorefetcher.mohahtn.xyz/"
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun observeImportEvents(keyHash: String): Flow<MaimaiImportEvent> = flow {
        client.prepareGet("${baseUrl}imports/$keyHash/events") {
            header("X-API-KEY", scorefetcherApiKey)
            header("Accept", "text/event-stream")
            timeout {
                requestTimeoutMillis = Long.MAX_VALUE
                connectTimeoutMillis = 10000
                socketTimeoutMillis = Long.MAX_VALUE
            }
        }.execute { response ->
            val channel = response.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                if (line.startsWith("data:")) {
                    val jsonData = line.removePrefix("data:").trim()
                    try {
                        val event = json.decodeFromString<MaimaiImportEvent>(jsonData)
                        emit(event)
                    } catch (e: Exception) {
                        PlatformUtils.log("ImportService", "Error decoding event: ${e.message}", true)
                    }
                }
            }
        }
    }
}
