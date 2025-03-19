package xyz.jimh.souschef.data

import org.junit.jupiter.api.Test

class AUnitTest {

    @Test
    fun identTest() {
        val idents = listOf(
            AUnit.Ident(5, UnitType.NONE),
            AUnit.Ident(6, UnitType.WEIGHT),
            AUnit.Ident(7, UnitType.VOLUME),
        )
    }
}