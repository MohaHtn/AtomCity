package org.arcade.atomcity.data.remote.model.scorefetcher

import kotlinx.serialization.Serializable

@Serializable
data class PlayerRankProgression(
    val keyHash: String? = null,
    val userName: String? = null,
    val playerName: String? = null,
    val name: String? = null,
    val rating: Int? = null,
    val isPublic: Boolean? = true,
    val totalPlayed: Int? = null,
    val totalCleared: Int? = null,
    val played: Int? = null,
    val cleared: Int? = null,
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
    val app: Int? = null
) {
    fun getDisplayName(): String {
        return userName ?: playerName ?: name ?: "Joueur anonyme"
    }

    fun getNormalizedRankCounts(): Map<String, Int> {
        if (!rankCounts.isNullOrEmpty()) return rankCounts
        if (!ranks.isNullOrEmpty()) return ranks

        val map = mutableMapOf<String, Int>()
        sssPlus?.let { if (it > 0) map["SSS+"] = it }
        sss?.let { if (it > 0) map["SSS"] = it }
        ssPlus?.let { if (it > 0) map["SS+"] = it }
        ss?.let { if (it > 0) map["SS"] = it }
        sPlus?.let { if (it > 0) map["S+"] = it }
        s?.let { if (it > 0) map["S"] = it }
        aaa?.let { if (it > 0) map["AAA"] = it }
        aa?.let { if (it > 0) map["AA"] = it }
        a?.let { if (it > 0) map["A"] = it }
        fc?.let { if (it > 0) map["FC"] = it }
        fcp?.let { if (it > 0) map["FC+"] = it }
        ap?.let { if (it > 0) map["AP"] = it }
        app?.let { if (it > 0) map["AP+"] = it }
        return map
    }

    fun getPlayedCount(): Int {
        val calculated = getNormalizedRankCounts().values.sum()
        val p = totalPlayed ?: played
        return if (p != null && p > 0) p else if (calculated > 0) calculated else (totalCleared ?: cleared ?: 0)
    }

    fun getClearedCount(): Int {
        val calculated = getNormalizedRankCounts().values.sum()
        val c = totalCleared ?: cleared
        return if (c != null && c > 0) c else calculated
    }
}

@Serializable
data class RankProgressionResponse(
    val totalGameCharts: Int = 0,
    val player: PlayerRankProgression? = null,
    val players: List<PlayerRankProgression>? = null
) {
    fun getAllPlayers(): List<PlayerRankProgression> {
        if (!players.isNullOrEmpty()) return players
        if (player != null) return listOf(player)
        return emptyList()
    }
}
