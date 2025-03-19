package xyz.jimh.souschef.config

import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

class UnitAbbrevTest {

    @Test
    fun values() {
        assertAll(
            Executable{ assertEquals("Full Name", UnitAbbrev.FULL_NAME.getLabel()) },
            Executable{ assertEquals("Abbreviation", UnitAbbrev.ABBREVIATION.getLabel()) },
        )

    }
}