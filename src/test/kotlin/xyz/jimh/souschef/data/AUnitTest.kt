package xyz.jimh.souschef.data

import kotlin.test.assertEquals
import kotlin.test.assertTrue
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

    @Test
    fun selfEqualityDoesNotRecurse() {
        val unit = AUnit(1, "cup", UnitType.VOLUME, 236.5882365, false, "c.")
        assertTrue(unit == unit)
    }
}
