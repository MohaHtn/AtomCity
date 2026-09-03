package org.arcade.atomcity.ui.game.taiko.settings

import org.arcade.atomcity.data.remote.model.taikoserver.TaikoImagesData

fun findDifficultySettingCourse(id: String): Int? = when (id) {
    "Désactivé" -> 0
    "Configurer à chaque fois" -> 1
    "Normal" -> 2
    "Difficile" -> 3
    "Oni" -> 4
    "Ura" -> 5
    else -> null
}

fun getCourseName(id: Int?): String = when (id) {
    0 -> "Désactivé"
    1 -> "Configurer à chaque fois"
    2 -> "Normal"
    3 -> "Difficile"
    4 -> "Oni"
    5 -> "Ura"
    else -> "Désactivé"
}

fun getAchievementDisplayDifficultyName(id: Int?): String = when (id) {
    0 -> "Désactivé"
    1 -> "Facile"
    2 -> "Normal"
    3 -> "Difficile"
    4 -> "Oni"
    5 -> "Oni/Ura"
    else -> "Désactivé"
}

fun findAchievementDisplayDifficultyId(name: String): Int? = when (name) {
    "Désactivé" -> 0
    "Facile" -> 1
    "Normal" -> 2
    "Difficile" -> 3
    "Oni" -> 4
    "Oni/Ura" -> 5
    else -> null
}

fun getAchievementRankPanelUrl(id: Int?): String? {
    val filename = when (id) {
        1 -> "rank_panel_Easy.webp"
        2 -> "rank_panel_Normal.webp"
        3 -> "rank_panel_Hard.webp"
        4 -> "rank_panel_Oni.webp"
        5 -> "rank_panel_Ura_Oni.webp"
        else -> null
    }
    return buildImageUrl("rank_panel", filename)
}

fun findTone(id: String?): Int? = when (id) {
    "Taiko" -> 0
    "Festival" -> 1
    "Dogs & Cats" -> 2
    "Taiko Deluxe" -> 3
    "Drumset" -> 4
    "Tambourine" -> 5
    "Wadadon" -> 6
    "Clapping" -> 7
    "Conga" -> 8
    "8-bit Taiko" -> 9
    "Heave-ho" -> 10
    "Mecha Don" -> 11
    "Funassyi" -> 12
    "Rap" -> 13
    "Hosogai" -> 14
    "Akemi" -> 15
    "Synth Drum" -> 16
    "Shuriken" -> 17
    "Bubble Pop" -> 18
    "Electric Guitar" -> 19
    else -> null
}

fun getToneName(id: Int?): String = when (id) {
    0 -> "Taiko"
    1 -> "Festival"
    2 -> "Dogs & Cats"
    3 -> "Deluxe Taiko"
    4 -> "Drumset"
    5 -> "Tambourine"
    6 -> "Wadadon"
    7 -> "Clapping"
    8 -> "Conga"
    9 -> "8-bit Taiko"
    10 -> "Heave-ho"
    11 -> "Mecha Don"
    12 -> "Funassyi"
    13 -> "Rap"
    14 -> "Hosogai"
    15 -> "Akemi"
    16 -> "Synth Drum"
    17 -> "Shuriken"
    18 -> "Bubble Pop"
    19 -> "Electric Guitar"
    else -> "Taiko"
}

fun findDifficultySettingSort(id: String): Int? = when (id) {
    "★ 1" -> 0
    "★ 2" -> 1
    "★ 3" -> 2
    "★ 4" -> 3
    "★ 5" -> 4
    "★ 6" -> 5
    "★ 7" -> 6
    "★ 8" -> 7
    "★ 9" -> 8
    "★ 10" -> 9
    else -> null
}

fun getSortName(id: Int?): String = if (id != null && id in 0..9) "★ ${id + 1}" else "★ 1"

fun findDifficultySettingStar(id: String): Int? = when (id) {
    "Désactivé" -> 0
    "Configurer à chaque fois" -> 1
    "Défaut" -> 2
    "Pas Clear" -> 3
    "Pas Full Combo" -> 4
    "Pas Donderful Combo" -> 5
    else -> null
}

fun getStarName(id: Int?): String = when (id) {
    0 -> "Désactivé"
    1 -> "Configurer à chaque fois"
    2 -> "Défaut"
    3 -> "Pas Clear"
    4 -> "Pas Full Combo"
    5 -> "Pas Donderful Combo"
    else -> "Désactivé"
}

fun getSpeedName(id: Int?): String = when (id) {
    0 -> "x1.0"
    1 -> "x1.1"
    2 -> "x1.2"
    3 -> "x1.3"
    4 -> "x1.4"
    5 -> "x1.5"
    6 -> "x1.6"
    7 -> "x1.7"
    8 -> "x1.8"
    9 -> "x1.9"
    10 -> "x2.0"
    11 -> "x2.5"
    12 -> "x3.0"
    13 -> "x3.5"
    14 -> "x4.0"
    else -> "x1.0"
}

fun findFilename(type: String, id: Int?, imagesData: TaikoImagesData?): String? {
    if (id == null || imagesData == null) return null
    val files = when (type) {
        "kigurumi" -> imagesData.images.costumes?.kigurumi?._files
        "head" -> imagesData.images.costumes?.head?._files
        "body" -> imagesData.images.costumes?.body?._files
        "face" -> imagesData.images.costumes?.face?._files
        "puchi" -> imagesData.images.costumes?.puchi?._files
        "speed" -> imagesData.images.speed?._files
        else -> null
    }
    return files?.find { extractId(it) == id }
}

fun findPlateFilename(id: Int?, imagesData: TaikoImagesData?): String? {
    if (id == null || imagesData == null) return null
    val suffix = when (id) {
        0 -> "Wood"
        1 -> "Rainbow"
        2 -> "Gold"
        3 -> "Purple"
        in 4..7 -> "AI_${id - 3}"
        8 -> "Onp_1"
        9 -> "Toho_Y22_QR"
        in 10..14 -> "Toho_Y22_${id - 9}"
        in 15..20 -> "AprilFool_${id - 14}"
        else -> null
    }
    if (suffix == null) return "nameplate.webp"
    val target = "nameplate_$suffix.webp"
    return imagesData.images.nameplates?._files?.find { it == target } ?: "nameplate.webp"
}

fun buildImageUrl(type: String, filename: String?): String? {
    if (filename == null) return null
    val baseUrl = "https://taiko.farewell.dev/images/"
    return when (type) {
        "kigurumi", "head", "body", "face", "puchi" -> "${baseUrl}Costumes/$type/$filename"
        "speed" -> "${baseUrl}Speed/$filename"
        "title" -> "${baseUrl}Nameplates/$filename"
        "random" -> "${baseUrl}$filename"
        else -> "${baseUrl}$filename"
    }
}

fun getItemDisplayName(type: String, item: String): String {
    return when (type) {
        "speed" -> "Vitesse ${getSpeedName(extractId(item))}"
        "random" -> when(item) {
            "Random_Whimsical.png" -> "Capricieux"
            "Random_Messy.png" -> "Chaotique"
            else -> "Normal"
        }
        "kigurumi", "head", "body", "face", "puchi" -> "ID: ${extractId(item)}"
        "title" -> if (item.startsWith("nameplate_")) {
            item.substringAfter("nameplate_").substringBefore(".").replace("_", " ")
        } else "Défaut"
        else -> item
    }
}
