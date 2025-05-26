package xyz.jimh.souschef.config

import kotlin.test.assertEquals
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UnitPreferenceTest {

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun getLabel() {
        UnitPreference.entries.forEach {
            assertEquals(it.name.toTitleCase(), it.getLabel())
        }
        assertAll(
            { assertEquals(UnitPreference.INTERNATIONAL, UnitPreference.valueOf("INTERNATIONAL")) },
            { assertEquals(UnitPreference.ENGLISH, UnitPreference.valueOf("ENGLISH")) },
            { assertEquals(UnitPreference.ANY, UnitPreference.valueOf("ANY")) },
            { assertEquals(3, UnitPreference.entries.size) },
            { assertThrows<IllegalArgumentException> { UnitPreference.valueOf("foo") } },
        )
    }
}

fun String.toTitleCase(): String =
    this.toLowerCaseAsciiOnly().capitalizeAsciiOnly()