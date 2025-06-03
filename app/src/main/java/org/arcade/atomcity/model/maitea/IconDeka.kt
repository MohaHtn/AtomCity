package org.arcade.atomcity.model.maitea

import com.squareup.moshi.Json

data class IconDeka (

    @Json(name = "id"      ) var id     : Int?     = null,
    @Json(name = "is_deka" ) var isDeka : Boolean? = null,
    @Json(name = "png"     ) var png    : String?  = null,
    @Json(name = "webp"    ) var webp   : String?  = null

)
