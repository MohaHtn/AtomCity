package org.arcade.atomcity.data.remote.model.taikoserver.usersettings

import kotlinx.serialization.Serializable

@Serializable
data class TaikoServerUserSettingsResponse(
    val baid: Int? = null,
    val toneId: Int? = null,
    val isDisplayAchievement: Boolean? = null,
    val isDisplayDanOnNamePlate: Boolean? = null,
    val isDisplaySouUchi: Boolean? = null,
    val difficultySettingCourse: Int? = null,
    val difficultySettingStar: Int? = null,
    val difficultySettingSort: Int? = null,
    val isVoiceOn: Boolean? = null,
    val isSkipOn: Boolean? = null,
    val achievementDisplayDifficulty: Int? = null,
    val playSetting: PlaySetting? = null,
    val notesPosition: Int? = null,
    val myDonName: String? = null,
    val myDonNameLanguage: Int? = null,
    val title: String? = null,
    val titlePlateId: Int? = null,
    val kigurumi: Int? = null,
    val head: Int? = null,
    val body: Int? = null,
    val face: Int? = null,
    val puchi: Int? = null,
    val unlockedKigurumi: List<Int> = emptyList(),
    val unlockedHead: List<Int> = emptyList(),
    val unlockedBody: List<Int> = emptyList(),
    val unlockedFace: List<Int> = emptyList(),
    val unlockedPuchi: List<Int> = emptyList(),
    val unlockedTitle: List<Int> = emptyList(),
    val faceColor: Int? = null,
    val bodyColor: Int? = null,
    val limbColor: Int? = null,
    val lastPlayDateTime: String? = null
)
