package xyz.jimh.souschef.data

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

class UnitBaseTest {

    @Test
    fun `equals and hashCode`() {
        assertAll(
            Executable { assertTrue(first == second) },
            Executable { assertTrue(first.hashCode() == second.hashCode()) },
        )

        first.name = "ml"
        assertAll(
            Executable { assertFalse(first == second) },
            Executable { assertFalse(first.hashCode() == second.hashCode()) },
        )

        first.name = second.name
        second.inBase = 1.1
        assertAll(
            Executable { assertFalse(first == second) },
            Executable { assertFalse(first.hashCode() == second.hashCode()) },
        )

        second.inBase = first.inBase
        first.intl = false
        assertAll(
            Executable { assertFalse(first == second) },
            Executable { assertFalse(first.hashCode() == second.hashCode()) },
        )

        first.intl = true
        first.abbrev = "ml."
        assertAll(
            Executable { assertFalse(first == second) },
            Executable { assertFalse(first.hashCode() == second.hashCode()) },
        )

        first.abbrev = second.abbrev
        first.id = 1L
        assertAll(
            Executable { assertFalse(first == second) },
            Executable { assertFalse(first.hashCode() == second.hashCode()) },
        )
    }

    companion object {
        val first = Volume("milliliter", 1.0, true, "ml")
        val second = Volume("milliliter", 1.0, true, "ml")
    }
}