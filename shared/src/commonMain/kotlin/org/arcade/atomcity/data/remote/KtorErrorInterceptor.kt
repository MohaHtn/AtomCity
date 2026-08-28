package org.arcade.atomcity.data.remote

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Ktor equivalent of the OkHttp ErrorInterceptor.
 * This can be installed in the HttpClient configuration using [installErrorValidator].
 */
fun HttpClientConfig<*>.installErrorValidator(errorHandler: NetworkErrorHandler) {
    HttpResponseValidator {
        validateResponse { response ->
            val statusCode = response.status
            if (statusCode.value < 300) return@validateResponse

            val url = response.request.url.toString()
            
            // We can use a KMP logging solution here if needed, or simple println
            println("HTTP ${statusCode.value} on $url")

            // On ignore les 404 sur les scores car c'est géré par un fallback (MaiTea)
            // On ignore aussi les 404 sur les imports car cela peut signifier qu'aucun import n'est actif
            if (statusCode == HttpStatusCode.NotFound && (url.contains("/scores") || url.contains("/player/details") || url.contains("/imports/"))) {
                return@validateResponse
            }

            val message = when (statusCode) {
                HttpStatusCode.NotFound -> "Ressource non trouvée. (404) : $url"
                HttpStatusCode.BadGateway -> "Le serveur est temporairement indisponible. (Bad Gateway 502)"
                HttpStatusCode.InternalServerError -> "Erreur interne du serveur. (500)"
                HttpStatusCode.Unauthorized -> "Non autorisé (401). Vérifiez votre clé API."
                HttpStatusCode.Forbidden -> "Accès refusé (403)."
                HttpStatusCode.Conflict -> {
                    try {
                        val body = response.bodyAsText()
                        Json.parseToJsonElement(body).jsonObject["message"]?.jsonPrimitive?.content ?: "Conflit (409)"
                    } catch (e: Exception) {
                        "Conflit (409)"
                    }
                }
                else -> {
                    try {
                        val body = response.bodyAsText()
                        Json.parseToJsonElement(body).jsonObject["message"]?.jsonPrimitive?.content ?: "Une erreur réseau est survenue (${statusCode.value}) : $url"
                    } catch (e: Exception) {
                        "Une erreur réseau est survenue (${statusCode.value}) : $url"
                    }
                }
            }
            
            errorHandler.onError(message)
            throw Exception(message)
        }
    }
}
