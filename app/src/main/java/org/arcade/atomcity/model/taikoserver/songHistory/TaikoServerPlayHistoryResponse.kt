package org.arcade.atomcity.model.taikoserver.songHistory

import com.squareup.moshi.Json

data class TaikoServerPlayHistoryResponse (
  @Json(name = "songHistoryData" ) var taikoServerSongHistoryData : List<TaikoServerSongHistory> = emptyList()

)