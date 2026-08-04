package org.arcade.atomcity.model.taikoserver.songHistory

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class TaikoServerPlayHistoryResponse (
  @SerialName( "songHistoryData" ) var taikoServerSongHistoryData : List<TaikoServerSongHistory> = emptyList()

)