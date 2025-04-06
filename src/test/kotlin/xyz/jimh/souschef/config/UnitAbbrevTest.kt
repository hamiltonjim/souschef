package xyz.jimh.souschef.config

import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.function.Executable

class UnitAbbrevTest {

    @Test
    fun values() {
        assertAll(
            Executable { assertEquals("Full Name", UnitAbbrev.FULL_NAME.getLabel()) },
            Executable { assertEquals("Abbreviation", UnitAbbrev.ABBREVIATION.getLabel()) },
            Executable { assertEquals(2, UnitAbbrev.values().size) },
            Executable { assertEquals( UnitAbbrev.FULL_NAME, UnitAbbrev.valueOf("FULL_NAME") ) },
            Executable { assertEquals( UnitAbbrev.ABBREVIATION, UnitAbbrev.valueOf("ABBREVIATION") ) },
            Executable { assertThrows<IllegalArgumentException> { UnitAbbrev.valueOf("foo") } },
        )
    }
}