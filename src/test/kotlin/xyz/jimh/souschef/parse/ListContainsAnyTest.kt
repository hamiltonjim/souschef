package xyz.jimh.souschef.parse

import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class ListContainsAnyTest {
    @Test
    fun `test when no intersection`() {
        assertFalse(americanEast.containsAny(nationalEast))
    }

    @Test
    fun `test when intersection with mixed case`() {
        assertAll(
            { assertFalse(nationalEast.containsAny(nlcs2024, ignoreCase = false)) },
            { assertTrue(nationalEast.containsAny(nlcs2024, ignoreCase = true)) },
        )
    }

    @Test
    fun `multiple intersections`() {
        assertAll(
            { assertTrue(nationalEast.containsAny(nlPlayoffs2024, ignoreCase = true)) },
            { assertFalse(nationalEast.containsAny(nlPlayoffs2024, ignoreCase = false)) },
        )
    }

    @Test
    fun `empty lists`() {
        assertAll(
            { assertFalse(nationalEast.containsAny(emptyList())) },
            { assertFalse(emptyList<String>().containsAny(nationalEast)) },
            { assertFalse(emptyList<String>().containsAny(emptyList())) },
        )
    }

    companion object {
        val americanEast = listOf(
            "red sox",
            "yankees",
            "orioles",
            "blue jays",
            "rays"
        )

        val nationalEast = listOf(
            "mets",
            "phillies",
            "nationals",
            "braves",
            "marlins"
        )

        val nlcs2024 = listOf("Mets", "Dodgers")
        val nlPlayoffs2024 = listOf(
            "Dodgers",
            "Phillies",
            "Brewers",
            "Padres",
            "Braves",
            "Mets"
        )
    }
}