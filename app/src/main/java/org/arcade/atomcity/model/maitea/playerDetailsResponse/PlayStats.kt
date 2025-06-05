package org.arcade.atomcity.model.maitea.playerDetailsResponse

import com.google.gson.annotations.SerializedName


data class PlayStats (

    @SerializedName("total"  ) var total  : Int?    = null,
    @SerializedName("wins"   ) var wins   : Int?    = null,
    @SerializedName("vs"     ) var vs     : Int?    = null,
    @SerializedName("sync"   ) var sync   : Int?    = null,
    @SerializedName("first"  ) var first  : First?  = First(),
    @SerializedName("latest" ) var latest : Latest? = Latest()

)