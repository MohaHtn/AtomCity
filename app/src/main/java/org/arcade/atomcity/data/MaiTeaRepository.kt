package org.arcade.atomcity.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.arcade.atomcity.domain.usecase.GetMaiTeaDataUseCase
import org.arcade.atomcity.model.maitea.playsResponse.MaiteaPlaysResponse
import org.arcade.atomcity.model.maitea.playerDetailsResponse.MaiteaPlayerDetailsResponse

class MaiTeaRepository(private val getMaiTeaDataUseCase: GetMaiTeaDataUseCase) {

    // Cache pour les données paginées
    private val playsCache = mutableMapOf<Int, MaiteaPlaysResponse>()

    // Cache pour les détails du joueur
    private var playerDetailsCache: MaiteaPlayerDetailsResponse? = null

    // Timestamp du dernier chargement des détails du joueur
    private var playerDetailsCacheTimestamp: Long = 0

    // Durée de validité du cache (10 minutes en millisecondes)
    private val CACHE_VALIDITY_DURATION = 10 * 60 * 1000L

    fun getMaiTeaPaginatedData(page: Int): Flow<MaiteaPlaysResponse?> = flow {
        // Vérifier si les données sont dans le cache
        playsCache[page]?.let {
            emit(it)
            return@flow
        }

        // Si non, faire l'appel API
        val response = getMaiTeaDataUseCase.execute(page)
        response?.let {
            // Mettre en cache les données
            playsCache[page] = it
        }
        emit(response)
    }

    fun getMaiTeaPlayerDetails(): Flow<MaiteaPlayerDetailsResponse?> = flow {
        val currentTime = System.currentTimeMillis()

        // Vérifier si les données en cache sont encore valides
        if (playerDetailsCache != null && currentTime - playerDetailsCacheTimestamp < CACHE_VALIDITY_DURATION) {
            emit(playerDetailsCache)
            return@flow
        }

        // Si non, faire l'appel API
        val response = getMaiTeaDataUseCase.executePlayerDetails()
        response?.let {
            // Mettre à jour le cache et le timestamp
            playerDetailsCache = it
            playerDetailsCacheTimestamp = currentTime
        }
        emit(response)
    }

    // Méthode pour vider le cache si nécessaire
    fun clearCache() {
        playsCache.clear()
        playerDetailsCache = null
        playerDetailsCacheTimestamp = 0
    }
}