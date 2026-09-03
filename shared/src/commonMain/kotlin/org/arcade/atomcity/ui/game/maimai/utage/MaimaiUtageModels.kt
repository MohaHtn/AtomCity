package org.arcade.atomcity.ui.game.maimai.utage

import org.arcade.atomcity.data.remote.model.maimai.UtageAttribute
import org.arcade.atomcity.data.remote.model.scorefetcher.playerBest30Response.PlayerBest30Response

enum class UtageAttributeCategory(val displayName: String) {
    PRE_DX("Pre-DX"),
    POST_DX("Post-DX"),
    OFF("Spéciaux"),
    UNUSED("Inutilisés")
}

data class CategorizedUtageAttribute(
    val attribute: UtageAttribute,
    val category: UtageAttributeCategory,
    val categoryName: String
)

data class UtageDisplayItem(
    val songTitle: String,
    val attribute: String?,
    val details: String?,
    val comment: String?,
    val forcedOptions: String?,
    val score: PlayerBest30Response?
)

data class ForcedOptionInfo(
    val optionName: String,
    val value: String
)

data class EraComment(
    val jp: String,
    val en: String? = null
)
