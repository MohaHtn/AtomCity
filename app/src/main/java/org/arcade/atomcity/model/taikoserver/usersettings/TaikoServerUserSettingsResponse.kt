package org.arcade.atomcity.model.taikoserver.usersettings

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDateTime


@JsonClass(generateAdapter = true)
data class TaikoServerUserSettingsResponse (

  @Json(name = "baid"                         ) var baid                         : Int?           = null,
  @Json(name = "toneId"                       ) var toneId                       : Int?           = null,
  @Json(name = "isDisplayAchievement"         ) var isDisplayAchievement         : Boolean?       = null,
  @Json(name = "isDisplayDanOnNamePlate"      ) var isDisplayDanOnNamePlate      : Boolean?       = null,
  @Json(name = "difficultySettingCourse"      ) var difficultySettingCourse      : Int?           = null,
  @Json(name = "difficultySettingStar"        ) var difficultySettingStar        : Int?           = null,
  @Json(name = "difficultySettingSort"        ) var difficultySettingSort        : Int?           = null,
  @Json(name = "isVoiceOn"                    ) var isVoiceOn                    : Boolean?       = null,
  @Json(name = "isSkipOn"                     ) var isSkipOn                     : Boolean?       = null,
  @Json(name = "achievementDisplayDifficulty" ) var achievementDisplayDifficulty : Int?           = null,
  @Json(name = "playSetting"                  ) var playSetting                  : PlaySetting?   = PlaySetting(),
  @Json(name = "notesPosition"                ) var notesPosition                : Int?           = null,
  @Json(name = "myDonName"                    ) var myDonName                    : String?        = null,
  @Json(name = "myDonNameLanguage"            ) var myDonNameLanguage            : Int?           = null,
  @Json(name = "title"                        ) var title                        : String?        = null,
  @Json(name = "titlePlateId"                 ) var titlePlateId                 : Int?           = null,
  @Json(name = "kigurumi"                     ) var kigurumi                     : Int?           = null,
  @Json(name = "head"                         ) var head                         : Int?           = null,
  @Json(name = "body"                         ) var body                         : Int?           = null,
  @Json(name = "face"                         ) var face                         : Int?           = null,
  @Json(name = "puchi"                        ) var puchi                        : Int?           = null,
  @Json(name = "unlockedKigurumi"             ) var unlockedKigurumi             : List<Int> = arrayListOf(),
  @Json(name = "unlockedHead"                 ) var unlockedHead                 : List<Int> = arrayListOf(),
  @Json(name = "unlockedBody"                 ) var unlockedBody                 : List<Int> = arrayListOf(),
  @Json(name = "unlockedFace"                 ) var unlockedFace                 : List<Int> = arrayListOf(),
  @Json(name = "unlockedPuchi"                ) var unlockedPuchi                : List<Int> = arrayListOf(),
  @Json(name = "unlockedTitle"                ) var unlockedTitle                : List<Int> = arrayListOf(),
  @Json(name = "faceColor"                    ) var faceColor                    : Int?           = null,
  @Json(name = "bodyColor"                    ) var bodyColor                    : Int?           = null,
  @Json(name = "limbColor"                    ) var limbColor                    : Int?           = null,
  @Json(name = "lastPlayDateTime"             ) var lastPlayDateTime              : String?       = null

)