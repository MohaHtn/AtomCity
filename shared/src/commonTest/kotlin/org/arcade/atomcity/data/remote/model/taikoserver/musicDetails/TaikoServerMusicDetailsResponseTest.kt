package org.arcade.atomcity.data.remote.model.taikoserver.musicDetails

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class TaikoServerMusicDetailsResponseTest {
    @Test
    fun decodesRootSongMap() {
        val response = Json.decodeFromString<TaikoServerMusicDetailsResponse>(
            """{"1":{"songId":1,"songName":"Test song"}}"""
        )

        assertEquals(1, response.entries["1"]?.songId)
        assertEquals("Test song", response.entries["1"]?.songName)
    }
}