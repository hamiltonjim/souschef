package xyz.jimh.souschef.config

import kotlin.test.assertEquals
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.function.Executable

class UnitPreferenceTest {

    @Test
    fun getLabel() {
        UnitPreference.values().forEach {
            assertEquals(it.name.toTitleCase(), it.getLabel())
        }
        assertAll(
            Executable { assertEquals(UnitPreference.INTERNATIONAL, UnitPreference.valueOf("INTERNATIONAL")) },
            Executable { assertEquals(UnitPreference.ENGLISH, UnitPreference.valueOf("ENGLISH")) },
            Executable { assertEquals(UnitPreference.ANY, UnitPreference.valueOf("ANY")) },
            Executable { assertThrows<IllegalArgumentException> { UnitPreference.valueOf("foo") } },
        )
    }
}

fun String.toTitleCase(): String =
    this.toLowerCaseAsciiOnly().capitalizeAsciiOnly()