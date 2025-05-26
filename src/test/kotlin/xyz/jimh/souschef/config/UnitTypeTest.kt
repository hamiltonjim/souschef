package xyz.jimh.souschef.config

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UnitTypeTest {
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `values and valueOf`() {
        assertAll(
            { assertEquals(UnitType.VOLUME, UnitType.valueOf("VOLUME") ) },
            { assertEquals(UnitType.WEIGHT, UnitType.valueOf("WEIGHT") ) },
            { assertEquals(UnitType.NONE, UnitType.valueOf("NONE") ) },
            { assertEquals(3, UnitType.entries.size ) },
            { assertThrows<IllegalArgumentException> { UnitType.valueOf("foo") }  },
            { assertEquals("VOLUME", UnitType.VOLUME.name ) },
            { assertEquals("WEIGHT", UnitType.WEIGHT.name ) },
            { assertEquals("NONE", UnitType.NONE.name ) },
        )
    }
}