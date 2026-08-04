package org.arcade.atomcity.data

import com.atomcity.maimai.db.AppDatabase
import com.atomcity.maimai.db.getAppDatabase
import com.atomcity.maimai.db.getDatabaseBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Repository that loads difficulties from the prepopulated Room DB and exposes lookup flows.
 */
class DifficultyRepository(private val database: AppDatabase) {
    private val _data = MutableStateFlow<Map<String, Map<String, String>>>(emptyMap())
    val data: Flow<Map<String, Map<String, String>>> = _data

    private fun normalizeTitle(title: String): String = title.trim().lowercase()

    private fun formatDifficulty(level: String?, internalLevel: String?): String {
        val cleanedLevel = level?.trim()?.takeIf { it.isNotEmpty() }
        val cleanedInternalLevel = internalLevel?.trim()?.takeIf { it.isNotEmpty() }

        return when {
            cleanedLevel != null && cleanedInternalLevel != null -> "$cleanedLevel | $cleanedInternalLevel"
            cleanedLevel != null -> cleanedLevel
            cleanedInternalLevel != null -> cleanedInternalLevel
            else -> "n/a"
        }
    }

    private fun difficultyName(diffIndex: Int?): String? {
        return when (diffIndex) {
            1 -> "easy"
            2 -> "basic"
            3 -> "advanced"
            4 -> "expert"
            5 -> "master"
            6 -> "remaster"
            else -> null
        }
    }

    suspend fun load() {
        if (_data.value.isNotEmpty()) return
        withContext(Dispatchers.IO) {
            try {
                val dao = database.songDao()
                val rows = dao.getAllSongLevels()

                val map = mutableMapOf<String, MutableMap<String, String>>()

                for (row in rows) {
                    val difficultyName = difficultyName(row.diffIndex) ?: continue
                    val formatted = formatDifficulty(row.level, row.internal_level)

                    val keys = listOf(
                        row.matchedTitle,
                        row.name_en,
                        row.name_jp,
                        row.code
                    )
                    .mapNotNull { it?.trim()?.takeIf { value -> value.isNotEmpty() } }
                    .map { normalizeTitle(it) }
                    .distinct()

                    for (key in keys) {
                        val difficulties = map.getOrPut(key) { mutableMapOf() }
                        difficulties[difficultyName] = formatted
                    }
                }

                _data.value = map
                println("DifficultyRepository: Loaded difficulties: ${map.size} songs")
            } catch (e: Exception) {
                println("DifficultyRepository: Failed to load DB data: ${e.message}")
            }
        }
    }

    fun getDifficultyFlow(songName: String): Flow<Map<String, String>> {
        val key = normalizeTitle(songName)
        return _data.map { map ->
            map[key] ?: emptyMap()
        }
    }

    private val repositoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun getDifficultyFlowOnDemand(songName: String): Flow<Map<String, String>> {
        if (_data.value.isEmpty()) {
            repositoryScope.launch {
                load()
            }
        }
        return getDifficultyFlow(songName)
    }

    /**
     * Récupère le niveau de difficulté et l'internal level pour une chanson et une difficulté données.
     * @param songId L'ID de la chanson
     * @param difficultyValue L'index de difficulté (1=easy, 2=basic, 3=advanced, 4=expert, 5=master, 6=remaster)
     * @return Un objet contenant level et internal_level, ou null si non trouvé
     */
    suspend fun getLevelByDifficulty(
        songId: Int,
        difficultyValue: Int
    ): LevelInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val dao = database.songDao()

                val levelEntity =
                    dao.getLevelByDifficulty(songId, difficultyValue) ?: return@withContext null

                levelEntity.let {
                    LevelInfo(
                        level = it.level,
                        internalLevel = it.internal_level,
                        diffIndex = it.diffIndex
                    )
                }
            } catch (e: Exception) {
                println("DifficultyRepository: Failed to get level for songId=$songId, difficultyValue=$difficultyValue: ${e.message}")
                null
            }
        }
    }
}

/**
 * Data class representing a Level.
 */
data class LevelInfo(
    val level: String?,
    val internalLevel: String?,
    val diffIndex: Int?
)
