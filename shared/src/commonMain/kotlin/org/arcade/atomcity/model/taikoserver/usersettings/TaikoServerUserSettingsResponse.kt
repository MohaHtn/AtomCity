package org.arcade.atomcity.model.taikoserver.usersettings

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class TaikoServerUserSettingsResponse (

  @SerialName( "baid"                         ) var baid                         : Int?           = null,
  @SerialName( "toneId"                       ) var toneId                       : Int?           = null,
  @SerialName( "isDisplayAchievement"         ) var isDisplayAchievement         : Boolean?       = null,
  @SerialName( "isDisplayDanOnNamePlate"      ) var isDisplayDanOnNamePlate      : Boolean?       = null,
  @SerialName( "difficultySettingCourse"      ) var difficultySettingCourse      : Int?           = null,
  @SerialName( "difficultySettingStar"        ) var difficultySettingStar        : Int?           = null,
  @SerialName( "difficultySettingSort"        ) var difficultySettingSort        : Int?           = null,
  @SerialName( "isVoiceOn"                    ) var isVoiceOn                    : Boolean?       = null,
  @SerialName( "isSkipOn"                     ) var isSkipOn                     : Boolean?       = null,
  @SerialName( "achievementDisplayDifficulty" ) var achievementDisplayDifficulty : Int?           = null,
  @SerialName( "playSetting"                  ) var playSetting                  : PlaySetting?   = PlaySetting(),
  @SerialName( "notesPosition"                ) var notesPosition                : Int?           = null,
  @SerialName( "myDonName"                    ) var myDonName                    : String?        = null,
  @SerialName( "myDonNameLanguage"            ) var myDonNameLanguage            : Int?           = null,
  @SerialName( "title"                        ) var title                        : String?        = null,
  @SerialName( "titlePlateId"                 ) var titlePlateId                 : Int?           = null,
  @SerialName( "kigurumi"                     ) var kigurumi                     : Int?           = null,
  @SerialName( "head"                         ) var head                         : Int?           = null,
  @SerialName( "body"                         ) var body                         : Int?           = null,
  @SerialName( "face"                         ) var face                         : Int?           = null,
  @SerialName( "puchi"                        ) var puchi                        : Int?           = null,
  @SerialName( "unlockedKigurumi"             ) var unlockedKigurumi             : List<Int> = arrayListOf(),
  @SerialName( "unlockedHead"                 ) var unlockedHead                 : List<Int> = arrayListOf(),
  @SerialName( "unlockedBody"                 ) var unlockedBody                 : List<Int> = arrayListOf(),
  @SerialName( "unlockedFace"                 ) var unlockedFace                 : List<Int> = arrayListOf(),
  @SerialName( "unlockedPuchi"                ) var unlockedPuchi                : List<Int> = arrayListOf(),
  @SerialName( "unlockedTitle"                ) var unlockedTitle                : List<Int> = arrayListOf(),
  @SerialName( "faceColor"                    ) var faceColor                    : Int?           = null,
  @SerialName( "bodyColor"                    ) var bodyColor                    : Int?           = null,
  @SerialName( "limbColor"                    ) var limbColor                    : Int?           = null,
  @SerialName( "lastPlayDateTime"             ) var lastPlayDateTime              : String?       = null

)