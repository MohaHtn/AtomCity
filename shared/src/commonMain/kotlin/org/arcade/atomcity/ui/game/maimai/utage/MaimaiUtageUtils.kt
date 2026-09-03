package org.arcade.atomcity.ui.game.maimai.utage

import org.arcade.atomcity.data.remote.model.maimai.UtageAttribute
import org.arcade.atomcity.data.remote.model.maimai.UtageData
import org.arcade.atomcity.data.remote.model.scorefetcher.playerBest30Response.PlayerBest30Response

val FORCED_OPTIONS_LEGEND = listOf(
    "Vitesse des notes",
    "Mode miroir",
    "Infos en fond",
    "Saut de piste",
    "Style de jugement"
)

fun parseForcedOptions(forcedOptionsStr: String?): List<ForcedOptionInfo> {
    if (forcedOptionsStr.isNullOrBlank()) return emptyList()
    val parts = forcedOptionsStr.split("/").map { it.trim() }
    
    val result = mutableListOf<ForcedOptionInfo>()
    for (i in parts.indices) {
        val valStr = parts[i]
        if (valStr != "-" && valStr.isNotEmpty() && i < FORCED_OPTIONS_LEGEND.size) {
            result.add(ForcedOptionInfo(FORCED_OPTIONS_LEGEND[i], valStr))
        }
    }
    return result
}

fun findUtageAttributeInfo(attributeKey: String?, staticData: UtageData?): UtageAttribute? {
    if (attributeKey.isNullOrBlank() || staticData?.chart_attributes == null) return null
    val key = attributeKey.replace("(", "").replace(")", "").trim()
    if (key.isEmpty()) return null

    val allAttributes = staticData.chart_attributes.pre_dx + 
                         staticData.chart_attributes.post_dx + 
                         staticData.chart_attributes.off_attributes + 
                         staticData.chart_attributes.unused_attributes

    val directMatch = allAttributes.find { attr ->
        val attrName = attr.attribute ?: ""
        attrName.startsWith(key) || attrName.contains(key)
    }
    if (directMatch != null) return directMatch

    if (key.length > 1 && key.contains("宴")) {
        val cleanKey = key.replace("宴", "").trim()
        if (cleanKey.isNotEmpty()) {
            return allAttributes.find { attr ->
                val attrName = attr.attribute ?: ""
                attrName.startsWith(cleanKey) || attrName.contains(cleanKey)
            }
        }
    }

    return null
}

fun normalizeString(s: String?): String = s?.lowercase()
    ?.replace(Regex("[^a-z0-9\u3040-\u309f\u30a0-\u30ff\u4e00-\u9faf\u4e00-\u9fff]"), "")
    ?.trim() ?: ""

fun parseUtageComment(rawCommentStr: String?): List<EraComment> {
    if (rawCommentStr.isNullOrBlank()) return emptyList()
    val raw = rawCommentStr.trim()

    val hasDash = raw.contains(" - ") || raw.contains(" — ") || raw.contains(" -") || raw.contains("- ")
    val parts = if (hasDash) raw.split(Regex("\\s+[-—]\\s+"), limit = 2) else listOf(raw)

    val jpRaw = parts.getOrNull(0)?.trim() ?: ""
    val enRaw = parts.getOrNull(1)?.trim()

    val jpList = jpRaw.split(Regex("\\s*/\\s*")).map { it.trim() }.filter { it.isNotEmpty() }
    val enList = enRaw?.split(Regex("\\s*/\\s*"))?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

    val result = mutableListOf<EraComment>()
    val maxLen = maxOf(jpList.size, enList.size)

    for (i in 0 until maxLen) {
        val jpStr = jpList.getOrNull(i) ?: ""
        val enStr = enList.getOrNull(i)
        
        if (jpStr.isNotEmpty() && !enStr.isNullOrEmpty() && jpStr != enStr) {
            result.add(EraComment(jp = jpStr, en = enStr))
        } else if (jpStr.isNotEmpty()) {
            result.add(EraComment(jp = jpStr, en = null))
        } else if (!enStr.isNullOrEmpty()) {
            result.add(EraComment(jp = enStr, en = null))
        }
    }

    return result
}

fun mergeUtageData(
    scores: List<PlayerBest30Response>,
    staticData: UtageData?
): List<UtageDisplayItem> {
    if (staticData == null) return scores.map { score ->
        UtageDisplayItem(
            songTitle = score.songJson?.name?.jp ?: score.songJson?.name?.en ?: "Unknown",
            attribute = score.difficultyLevelJson?.label,
            details = null,
            comment = null,
            forcedOptions = null,
            score = score
        )
    }

    val results = mutableListOf<UtageDisplayItem>()
    val matchedScoreIds = mutableSetOf<Int>()

    staticData.utage_chart_list.forEach { chartEntry ->
        if (chartEntry.variants != null) {
            chartEntry.variants.forEach { variant ->
                val matchingScore = findMatchingScore(chartEntry.song, variant.attribute, scores, matchedScoreIds)
                results.add(UtageDisplayItem(
                    songTitle = chartEntry.song,
                    attribute = variant.attribute,
                    details = variant.details_fr ?: chartEntry.details_fr,
                    comment = variant.comment ?: chartEntry.comment,
                    forcedOptions = variant.forced_options ?: chartEntry.forced_options,
                    score = matchingScore
                ))
                matchingScore?.playId?.let { matchedScoreIds.add(it) }
            }
        } else {
            val matchingScore = findMatchingScore(chartEntry.song, chartEntry.attribute, scores, matchedScoreIds)
            results.add(UtageDisplayItem(
                songTitle = chartEntry.song,
                attribute = chartEntry.attribute,
                details = chartEntry.details_fr,
                comment = chartEntry.comment,
                forcedOptions = chartEntry.forced_options,
                score = matchingScore
            ))
            matchingScore?.playId?.let { matchedScoreIds.add(it) }
        }
    }

    scores.filter { it.playId != null && it.playId !in matchedScoreIds }.forEach { score ->
        results.add(UtageDisplayItem(
            songTitle = score.songJson?.name?.jp ?: score.songJson?.name?.en ?: "Unknown",
            attribute = score.difficultyLevelJson?.label,
            details = null,
            comment = null,
            forcedOptions = null,
            score = score
        ))
    }

    return results.sortedWith(
        compareByDescending<UtageDisplayItem> { it.score?.achievement ?: -1.0 }
            .thenBy { it.songTitle }
    )
}

fun findMatchingScore(
    jsonSong: String, 
    jsonAttribute: String?, 
    scores: List<PlayerBest30Response>,
    usedIds: Set<Int>
): PlayerBest30Response? {
    val normFullJson = normalizeString(jsonSong)
    
    val jsonParts = if (jsonSong.contains(" - ") || jsonSong.contains(" -") || jsonSong.contains("- ")) {
        jsonSong.split(Regex("\\s*-\\s*"))
    } else {
        listOf(jsonSong)
    }
    
    val jsonJpPart = jsonParts.getOrNull(0)?.trim() ?: jsonSong
    val jsonEnPart = jsonParts.getOrNull(1)?.trim()
    
    val normJsonJp = normalizeString(jsonJpPart)
    val normJsonEn = jsonEnPart?.let { normalizeString(it) }
    
    val rawJsonAttr = jsonAttribute?.replace("(", "")?.replace(")", "")?.trim() ?: ""

    val exactMatch = scores.find { score ->
        if (score.playId != null && score.playId in usedIds) return@find false

        val apiJp = normalizeString(score.songJson?.name?.jp)
        val apiEn = normalizeString(score.songJson?.name?.en)
        
        val isExact = (normFullJson.isNotEmpty() && (normFullJson == apiJp || normFullJson == apiEn)) ||
                      (normJsonJp.isNotEmpty() && (normJsonJp == apiJp || normJsonJp == apiEn)) ||
                      (!normJsonEn.isNullOrEmpty() && (normJsonEn == apiEn || normJsonEn == apiJp))

        if (!isExact) return@find false

        val apiAttr = score.difficultyLevelJson?.label?.replace("(", "")?.replace(")", "")?.trim() ?: ""
        
        when {
            rawJsonAttr.isNotEmpty() && apiAttr.isNotEmpty() && rawJsonAttr == apiAttr -> true
            apiAttr == "宴" || apiAttr.isEmpty() -> true
            rawJsonAttr == "宴" || rawJsonAttr.isEmpty() -> true
            else -> true
        }
    }

    if (exactMatch != null) return exactMatch

    return scores.find { score ->
        if (score.playId != null && score.playId in usedIds) return@find false

        val apiJp = normalizeString(score.songJson?.name?.jp)
        val apiEn = normalizeString(score.songJson?.name?.en)

        val isContainsJp = normJsonJp.length > 3 && apiJp.isNotEmpty() && (apiJp.contains(normJsonJp) || normJsonJp.contains(apiJp))
        val isContainsEn = !normJsonEn.isNullOrEmpty() && normJsonEn.length > 3 && apiEn.isNotEmpty() && (apiEn.contains(normJsonEn) || normJsonEn.contains(apiEn))

        if (!isContainsJp && !isContainsEn) return@find false

        val apiAttr = score.difficultyLevelJson?.label?.replace("(", "")?.replace(")", "")?.trim() ?: ""
        
        when {
            rawJsonAttr.isNotEmpty() && apiAttr.isNotEmpty() && rawJsonAttr == apiAttr -> true
            apiAttr == "宴" || apiAttr.isEmpty() -> true
            rawJsonAttr == "宴" || rawJsonAttr.isEmpty() -> true
            else -> true
        }
    }
}
