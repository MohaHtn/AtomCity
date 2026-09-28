package org.arcade.atomcity.data.remote.model.scorefetcher

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
data class PlayerRankProgression(
    val keyHash: String? = null,
    val userName: String? = null,
    val playerName: String? = null,
    val name: String? = null,
    val rating: Int? = null,
    val isPublic: Boolean? = true,
    val isProgressionPublic: Boolean? = null,
    val completionPercentage: Double? = null,
    val totalPlayed: Int? = null,
    val totalCleared: Int? = null,
    val played: Int? = null,
    val cleared: Int? = null,
    val totalSongs: Int? = null,
    val playedSongs: Int? = null,
    val rankCounts: Map<String, Int>? = null,
    val ranks: Map<String, Int>? = null,
    val clearCounts: Map<String, Int>? = null,
    val sssPlus: Int? = null,
    val sss: Int? = null,
    val ssPlus: Int? = null,
    val ss: Int? = null,
    val sPlus: Int? = null,
    val s: Int? = null,
    val aaa: Int? = null,
    val aa: Int? = null,
    val a: Int? = null,
    val fc: Int? = null,
    val fcp: Int? = null,
    val ap: Int? = null,
    val app: Int? = null,
    val fcCount: Int? = null,
    val fcPlusCount: Int? = null,
    val apCount: Int? = null,
    val apPlusCount: Int? = null
) {
    val isPublicEffective: Boolean
        get() = isProgressionPublic ?: isPublic ?: true

    fun getDisplayName(): String {
        return userName ?: playerName ?: name ?: "Joueur anonyme"
    }

    fun getNormalizedRankCounts(): Map<String, Int> {
        val rawMap = rankCounts?.takeIf { it.isNotEmpty() } ?: ranks?.takeIf { it.isNotEmpty() }
        val map = mutableMapOf<String, Int>()
        if (rawMap != null) {
            for ((k, v) in rawMap) {
                val key = when (k.trim().lowercase()) {
                    "sss+", "sss_plus", "sssplus" -> "SSS+"
                    "sss" -> "SSS"
                    "ss+", "ss_plus", "ssplus" -> "SS+"
                    "ss" -> "SS"
                    "s+", "s_plus", "splus" -> "S+"
                    "s" -> "S"
                    "aaa" -> "AAA"
                    "aa" -> "AA"
                    "a" -> "A"
                    "autres", "autres_ranks", "autresranks", "other", "others" -> "Autres"
                    "fc" -> "FC"
                    "fc+", "fcp", "fc_plus" -> "FC+"
                    "ap" -> "AP"
                    "ap+", "app", "ap_plus" -> "AP+"
                    else -> k.trim()
                }
                map[key] = (map[key] ?: 0) + v
            }
        }

        (fcCount ?: fc)?.let { if (it > 0 && !map.containsKey("FC")) map["FC"] = it }
        (fcPlusCount ?: fcp)?.let { if (it > 0 && !map.containsKey("FC+")) map["FC+"] = it }
        (apCount ?: ap)?.let { if (it > 0 && !map.containsKey("AP")) map["AP"] = it }
        (apPlusCount ?: app)?.let { if (it > 0 && !map.containsKey("AP+")) map["AP+"] = it }

        sssPlus?.let { if (it > 0 && !map.containsKey("SSS+")) map["SSS+"] = it }
        sss?.let { if (it > 0 && !map.containsKey("SSS")) map["SSS"] = it }
        ssPlus?.let { if (it > 0 && !map.containsKey("SS+")) map["SS+"] = it }
        ss?.let { if (it > 0 && !map.containsKey("SS")) map["SS"] = it }
        sPlus?.let { if (it > 0 && !map.containsKey("S+")) map["S+"] = it }
        s?.let { if (it > 0 && !map.containsKey("S")) map["S"] = it }
        aaa?.let { if (it > 0 && !map.containsKey("AAA")) map["AAA"] = it }
        aa?.let { if (it > 0 && !map.containsKey("AA")) map["AA"] = it }
        a?.let { if (it > 0 && !map.containsKey("A")) map["A"] = it }
        return map
    }

    fun getPlayedCount(): Int {
        val p = playedSongs ?: totalPlayed ?: played
        return if (p != null && p > 0) p else getClearedCount()
    }

    fun getClearedCount(): Int {
        val c = totalCleared ?: cleared
        if (c != null && c > 0) return c

        val rankKeys = setOf("SSS+", "SSS", "SS+", "SS", "S+", "S", "AAA", "AA", "A", "Autres")
        val sumFromRanks = getNormalizedRankCounts()
            .filterKeys { it in rankKeys }
            .values.sum()

        if (sumFromRanks > 0) return sumFromRanks

        val nonComboSum = getNormalizedRankCounts()
            .filterKeys { it !in setOf("FC", "FC+", "AP", "AP+") }
            .values.sum()

        return if (nonComboSum > 0) nonComboSum else getNormalizedRankCounts().values.sum()
    }
}

@Serializable
data class RankProgressionResponse(
    val totalGameCharts: Int = 0,
    val totalSongs: Int? = null,
    val playedSongs: Int? = null,
    val ranks: Map<String, Int>? = null,
    val rankCounts: Map<String, Int>? = null,
    val keyHash: String? = null,
    val userName: String? = null,
    val playerName: String? = null,
    val name: String? = null,
    val rating: Int? = null,
    val isPublic: Boolean? = true,
    val isProgressionPublic: Boolean? = null,
    val totalPlayed: Int? = null,
    val totalCleared: Int? = null,
    val played: Int? = null,
    val cleared: Int? = null,
    val player: PlayerRankProgression? = null,
    val players: List<PlayerRankProgression>? = null
) {
    val effectiveTotalGameCharts: Int
        get() {
            if (totalGameCharts > 0) return totalGameCharts
            val playersList = getAllPlayers()
            val hundredPctPlayer = playersList.find { (it.completionPercentage ?: 0.0) >= 100.0 }
            if (hundredPctPlayer != null) {
                val count = hundredPctPlayer.getClearedCount()
                if (count > 0) return count
            }
            for (p in playersList) {
                val pct = p.completionPercentage
                val cleared = p.getClearedCount()
                if (pct != null && pct > 0.0 && cleared > 0) {
                    val estimated = (cleared / (pct / 100.0)).roundToInt()
                    if (estimated > 0) return estimated
                }
            }
            return 0
        }

    fun getAllPlayers(): List<PlayerRankProgression> {
        if (!players.isNullOrEmpty()) return players
        if (player != null) return listOf(player)

        val mapRanks = ranks ?: rankCounts
        if (!mapRanks.isNullOrEmpty() || totalSongs != null || playedSongs != null) {
            return listOf(
                PlayerRankProgression(
                    keyHash = keyHash,
                    userName = userName,
                    playerName = playerName,
                    name = name,
                    rating = rating,
                    isPublic = isPublic ?: true,
                    isProgressionPublic = isProgressionPublic ?: isPublic,
                    totalPlayed = totalPlayed ?: played,
                    totalCleared = totalCleared ?: cleared,
                    played = played ?: totalPlayed,
                    cleared = cleared ?: totalCleared,
                    totalSongs = totalSongs,
                    playedSongs = playedSongs,
                    rankCounts = mapRanks,
                    ranks = mapRanks
                )
            )
        }
        return emptyList()
    }
}

