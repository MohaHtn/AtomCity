package org.arcade.atomcity.model.maitea.playsResponse

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Player (

  @SerialName( "id"             ) var id            : Int?       = null,
  @SerialName( "name"           ) var name          : String?    = null,
  @SerialName( "rating"         ) var rating        : Int?       = null,
  @SerialName( "rating_highest" ) var ratingHighest : Int?       = null,
  @SerialName( "level"          ) var level         : Int?       = null,
  @SerialName( "play_stats"     ) var playStats     : PlayStats? = PlayStats(),
  @SerialName( "options"        ) var options       : Options?   = Options()

)