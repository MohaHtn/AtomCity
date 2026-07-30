package org.arcade.atomcity.ui.game.maimai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AtomCityUsersTest {

    @Test
    fun extractAtomCityUserDisplay_usesValueAsRatingWhenNumeric() {
        val (username, rating) = extractAtomCityUserDisplay("Alice", "15432")

        assertEquals("Alice", username)
        assertEquals("15432", rating)
    }

    @Test
    fun extractAtomCityUserDisplay_usesKeyAsRatingWhenNumeric() {
        val (username, rating) = extractAtomCityUserDisplay("15432", "Alice")

        assertEquals("Alice", username)
        assertEquals("15432", rating)
    }

    @Test
    fun extractAtomCityUserDisplay_hidesRatingWhenNeitherSideIsNumeric() {
        val (username, rating) = extractAtomCityUserDisplay("keyHash", "Alice")

        assertEquals("Alice", username)
        assertNull(rating)
    }
}
