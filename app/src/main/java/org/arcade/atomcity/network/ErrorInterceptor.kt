package org.arcade.atomcity.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import org.arcade.atomcity.ui.core.GlobalUIState

class ErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        
        if (!response.isSuccessful) {
            val url = request.url.toString()
            Log.w("ErrorInterceptor", "HTTP ${response.code} on $url")

            // On ignore les 404 sur les scores car c'est géré par un fallback (MaiTea)
            // On ignore aussi les 404 sur les imports car cela peut signifier qu'aucun import n'est actif
            if (response.code == 404 && (url.contains("/scores") || url.contains("/player/details") || url.contains("/imports/"))) {
                return response
            }

            val message = when (response.code) {
                404 -> "Ressource non trouvée. (404) : $url"
                502 -> "Le serveur est temporairement indisponible. (Bad Gateway 502)"
                500 -> "Erreur interne du serveur. (500)"
                401 -> "Non autorisé (401). Vérifiez votre clé API."
                403 -> "Accès refusé (403)."
                else -> "Une erreur réseau est survenue (${response.code}) : $url"
            }
            
            GlobalUIState.globalError.value = message
        }
        
        return response
    }
}
