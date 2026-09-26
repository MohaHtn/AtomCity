package org.arcade.atomcity.ui.game.taiko.stats

data class TaikoMostPlayedEntry(
    val songId: Int,
    val musicName: String?,
    val musicNameEN: String? = null,
    val musicArtist: String?,
    val musicArtistEN: String? = null,
    val difficulty: Int? = null,
    val stars: Int? = null,
    val playCount: Int,
    val uniquePlayers: Int? = null,
    val userPlayCounts: Map<String, Int>? = null,
    val hasCurrentUserPlayed: Boolean = false
)

data class TaikoProgressStats(
    val difficulty: Int, // 1: Kantan, 2: Futsuu, 3: Muzukashii, 4: Oni, 5: Ura Oni
    val totalSongs: Int,
    val playedSongs: Int,
    val clearCount: Int,
    val fullComboCount: Int,
    val donderfulComboCount: Int, // Perfect
    val rankKiwamiCount: Int = 0,      // scoreRank 7 (極)
    val rankMiyabiGoldCount: Int = 0,   // scoreRank 4 (雅 Gold)
    val rankMiyabiPinkCount: Int = 0,   // scoreRank 5 (雅 Pink)
    val rankMiyabiPurpleCount: Int = 0, // scoreRank 6 (雅 Purple)
    val rankIkiWhiteCount: Int = 0,     // scoreRank 1 (粋 White)
    val rankIkiBronzeCount: Int = 0,    // scoreRank 2 (粋 Bronze)
    val rankIkiBlueCount: Int = 0       // scoreRank 3 (粋 Blue)
) {
    val completionRate: Float
        get() = if (totalSongs > 0) clearCount.toFloat() / totalSongs else 0f
}

data class TaikoOverallProgressStats(
    val totalSongs: Int,
    val playedSongs: Int,
    val clearCount: Int,       // Joué ET au moins Clear
    val fullComboCount: Int,
    val donderfulComboCount: Int
) {
    val completionRate: Float
        get() = if (totalSongs > 0) clearCount.toFloat() / totalSongs else 0f
}
