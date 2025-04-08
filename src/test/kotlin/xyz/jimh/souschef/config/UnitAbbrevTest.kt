package xyz.jimh.souschef.config

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.function.Executable

class UnitAbbrevTest {

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun values() {
        assertAll(
            Executable { assertNotNull(UnitAbbrev.FULL_NAME) },
            Executable { assertNotNull(UnitAbbrev.ABBREVIATION) },
            Executable { assertEquals("Full Name", UnitAbbrev.FULL_NAME.getLabel()) },
            Executable { assertEquals("Abbreviation", UnitAbbrev.ABBREVIATION.getLabel()) },
            Executable { assertEquals("FULL_NAME", UnitAbbrev.FULL_NAME.toString()) },
            Executable { assertEquals("ABBREVIATION", UnitAbbrev.ABBREVIATION.toString()) },
            Executable { assertEquals("FULL_NAME", UnitAbbrev.FULL_NAME.name) },
            Executable { assertEquals("ABBREVIATION", UnitAbbrev.ABBREVIATION.name) },
            Executable { assertEquals(2, UnitAbbrev.entries.size) },
            Executable { assertEquals( UnitAbbrev.FULL_NAME, UnitAbbrev.valueOf("FULL_NAME") ) },
            Executable { assertEquals( UnitAbbrev.ABBREVIATION, UnitAbbrev.valueOf("ABBREVIATION") ) },
            Executable { assertThrows<IllegalArgumentException> { UnitAbbrev.valueOf("foo") } },
        )
    }
}