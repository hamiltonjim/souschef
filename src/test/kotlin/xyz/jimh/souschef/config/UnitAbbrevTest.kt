package xyz.jimh.souschef.config

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UnitAbbrevTest {

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun values() {
        assertAll(
            { assertNotNull(UnitAbbrev.FULL_NAME) },
            { assertNotNull(UnitAbbrev.ABBREVIATION) },
            { assertEquals("Full Name", UnitAbbrev.FULL_NAME.getLabel()) },
            { assertEquals("Abbreviation", UnitAbbrev.ABBREVIATION.getLabel()) },
            { assertEquals("FULL_NAME", UnitAbbrev.FULL_NAME.toString()) },
            { assertEquals("ABBREVIATION", UnitAbbrev.ABBREVIATION.toString()) },
            { assertEquals("FULL_NAME", UnitAbbrev.FULL_NAME.name) },
            { assertEquals("ABBREVIATION", UnitAbbrev.ABBREVIATION.name) },
            { assertEquals(2, UnitAbbrev.entries.size) },
            { assertEquals( UnitAbbrev.FULL_NAME, UnitAbbrev.valueOf("FULL_NAME") ) },
            { assertEquals( UnitAbbrev.ABBREVIATION, UnitAbbrev.valueOf("ABBREVIATION") ) },
            { assertThrows<IllegalArgumentException> { UnitAbbrev.valueOf("foo") } },
        )
    }
}