package xyz.jimh.souschef.config

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.function.Executable

class UnitTypeTest {
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `values and valueOf`() {
        assertAll(
            Executable { assertEquals(UnitType.VOLUME, UnitType.valueOf("VOLUME") ) },
            Executable { assertEquals(UnitType.WEIGHT, UnitType.valueOf("WEIGHT") ) },
            Executable { assertEquals(UnitType.NONE, UnitType.valueOf("NONE") ) },
            Executable { assertEquals(3, UnitType.entries.size ) },
            Executable { assertThrows<IllegalArgumentException> { UnitType.valueOf("foo") }  },
            Executable { assertEquals("VOLUME", UnitType.VOLUME.name ) },
            Executable { assertEquals("WEIGHT", UnitType.WEIGHT.name ) },
            Executable { assertEquals("NONE", UnitType.NONE.name ) },
        )
    }
}