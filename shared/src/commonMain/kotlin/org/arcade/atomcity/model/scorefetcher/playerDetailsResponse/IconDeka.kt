package org.arcade.atomcity.model.scorefetcher.playerDetailsResponse

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class IconDeka (

    @SerialName( "id"      ) var id     : Int?     = null,
    @SerialName( "is_deka" ) var isDeka : Boolean? = null,
    @SerialName( "png"     ) var png    : String?  = null,
    @SerialName( "webp"    ) var webp   : String?  = null

)
