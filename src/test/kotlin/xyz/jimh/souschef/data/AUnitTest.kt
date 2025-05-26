package xyz.jimh.souschef.data

import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import xyz.jimh.souschef.config.UnitType

class AUnitTest {

    @Test
    fun identTest() {
        val idents = listOf(
            AUnit.Ident(5, UnitType.NONE),
            AUnit.Ident(6, UnitType.WEIGHT),
            AUnit.Ident(7, UnitType.VOLUME),
        )
        val one = idents[0].copy()
        assertEquals(idents[0], one)
    }
}