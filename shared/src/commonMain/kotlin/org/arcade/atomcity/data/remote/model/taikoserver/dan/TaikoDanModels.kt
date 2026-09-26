package org.arcade.atomcity.data.remote.model.taikoserver.dan

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class DanCourseData(
    @SerialName("danId") val danId: Int,
    @SerialName("verupNo") val verupNo: Int? = null,
    @SerialName("title") val title: String = "",
    @SerialName("aryOdaiSong") val aryOdaiSong: List<DanOdaiSong> = emptyList(),
    @SerialName("aryOdaiBorder") val aryOdaiBorder: List<DanOdaiBorder> = emptyList()
)

@Serializable
data class DanOdaiSong(
    @SerialName("songNo") val songNo: Int,
    @SerialName("level") val level: Int? = null,
    @SerialName("isHiddenSongName") val isHiddenSongName: Boolean? = false
)

@Serializable
data class DanOdaiBorder(
    @SerialName("odaiType") val odaiType: Int,
    @SerialName("borderType") val borderType: Int = 1,
    @SerialName("redBorderTotal") val redBorderTotal: Int = 0,
    @SerialName("goldBorderTotal") val goldBorderTotal: Int = 0
)

@Serializable
data class TaikoServerDanBestDataResponse(
    @SerialName("danBestDataList") val danBestDataList: List<TaikoDanBestCourseData> = emptyList()
)

@Serializable
data class TaikoDanBestCourseData(
    @SerialName("danId") val danId: Int,
    @SerialName("clearState") val clearState: Int? = 0,
    @SerialName("soulGaugeTotal") val soulGaugeTotal: Int? = 0,
    @SerialName("comboCountTotal") val comboCountTotal: Int? = 0,
    @SerialName("danBestStageDataList") val danBestStageDataList: List<TaikoDanBestStageData> = emptyList()
)

@Serializable
data class TaikoDanBestStageData(
    @SerialName("songNumber") val songNumber: Int? = 0,
    @SerialName("playScore") val playScore: Int? = 0,
    @SerialName("goodCount") val goodCount: Int? = 0,
    @SerialName("okCount") val okCount: Int? = 0,
    @SerialName("badCount") val badCount: Int? = 0,
    @SerialName("drumrollCount") val drumrollCount: Int? = 0,
    @SerialName("totalHitCount") val totalHitCount: Int? = 0,
    @SerialName("comboCount") val comboCount: Int? = 0,
    @SerialName("highScore") val highScore: Long? = 0
)
