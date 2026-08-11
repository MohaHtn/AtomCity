package org.arcade.atomcity.worker

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.arcade.atomcity.network.ImportService
import org.arcade.atomcity.utils.PlatformUtils

class IosImportWorkManager(private val importService: ImportService) : ImportWorkManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _progress = MutableStateFlow<ImportProgress?>(null)
    private val _isActive = MutableStateFlow(false)

    override fun startImport(apiKey: String) {
        if (_isActive.value) return
        
        val keyHash = PlatformUtils.sha256(apiKey)
        _isActive.value = true
        _progress.value = ImportProgress("running", 0, "Démarrage de l'import...")

        scope.launch {
            try {
                importService.observeImportEvents(keyHash).collect { event ->
                    when (event.type) {
                        "page" -> {
                            val progress = if ((event.totalPages ?: 0) > 0) {
                                (((event.page ?: 0).toFloat() / (event.totalPages ?: 1)) * 100).toInt().coerceIn(0, 100)
                            } else 0
                            _progress.value = ImportProgress("running", progress, "Importation: $progress% (Page ${event.page}/${event.totalPages})")
                        }
                        "completed" -> {
                            _progress.value = ImportProgress("succeeded", 100, "Importation terminée")
                            _isActive.value = false
                        }
                        "failed" -> {
                            _progress.value = ImportProgress("failed", 0, "Échec de l'importation")
                            _isActive.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                _progress.value = ImportProgress("failed", 0, "Erreur de connexion: ${e.message}")
                _isActive.value = false
            }
        }
    }

    override fun isImportActive(): Flow<Boolean> = _isActive

    override fun observeProgress(): Flow<ImportProgress?> = _progress
}
