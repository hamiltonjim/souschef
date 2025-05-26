package xyz.jimh.souschef.utility

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import xyz.jimh.souschef.utility.MathUtils.EPSILON
import xyz.jimh.souschef.utility.MathUtils.eqEpsilon
import xyz.jimh.souschef.utility.MathUtils.geEpsilon
import xyz.jimh.souschef.utility.MathUtils.gtEpsilon
import xyz.jimh.souschef.utility.MathUtils.leEpsilon
import xyz.jimh.souschef.utility.MathUtils.ltEpsilon
import xyz.jimh.souschef.utility.MathUtils.neEpsilon

class MathUtilsTest {

    @Test
    fun geEpsilonTest() {
        Assertions.assertAll(
            { Assertions.assertTrue(geEpsilon(1.0, 1.0 + EPSILON / 2.0), "ge actually < by epsilon / 2") },
            { Assertions.assertTrue(geEpsilon(1.0, 1.0), "ge actually ==") },
            { Assertions.assertFalse(geEpsilon(1.0, 1.0 + EPSILON * 2.0), "ge actually < by epsilon * 2 passes") }
        )
    }

    @Test
    fun gtEpsilonTest() {
        Assertions.assertAll(
            { Assertions.assertTrue(gtEpsilon(1.0, 1.0 + EPSILON / 2.0), "gt actually < by epsilon / 2") },
            { Assertions.assertTrue(gtEpsilon(1.0, 1.0), "gt actually ==") },
            { Assertions.assertFalse(gtEpsilon(1.0, 1.0 + EPSILON * 2.0), "gt actually < by epsilon * 2 passes") }
        )
    }

    @Test
    fun ltEpsilonTest() {
        Assertions.assertAll(
            { Assertions.assertTrue(ltEpsilon(1.0, 1.0 - EPSILON / 2.0), "lt actually < by epsilon / 2") },
            { Assertions.assertTrue(ltEpsilon(1.0, 1.0), "lt actually ==") },
            { Assertions.assertFalse(ltEpsilon(1.0, 1.0 - EPSILON * 2.0), "lt actually < by epsilon * 2 passes") }
        )
    }

    @Test
    fun leEpsilonTest() {
        Assertions.assertAll(
            { Assertions.assertTrue(leEpsilon(1.0, 1.0 - EPSILON / 2.0), "le actually < by epsilon / 2") },
            { Assertions.assertTrue(leEpsilon(1.0, 1.0), "le actually ==") },
            { Assertions.assertFalse(leEpsilon(1.0, 1.0 - EPSILON * 2.0), "le actually < by epsilon * 2 passes") }
        )
    }

    @Test
    fun eqEpsilonTest() {
        Assertions.assertAll(
            { Assertions.assertTrue(eqEpsilon(1.0, 1.0 + EPSILON / 2.0), "eq actually < by epsilon / 2") },
            { Assertions.assertTrue(eqEpsilon(1.0, 1.0), "le actually ==") },
            { Assertions.assertFalse(eqEpsilon(1.0, 1.0 + EPSILON * 2.0), "eq actually < by epsilon * 2 passes") },
            { Assertions.assertTrue(eqEpsilon(1.0, 1.0 - EPSILON / 2.0), "eq actually > by epsilon / 2") },
            { Assertions.assertFalse(eqEpsilon(1.0, 1.0 - EPSILON * 2.0), "eq actually > by epsilon * 2 passes") }
        )
    }

    @Test
    fun neEpsilonTest() {
        Assertions.assertAll(
            { Assertions.assertFalse(neEpsilon(1.0, 1.0 + EPSILON / 2.0), "ne actually < by epsilon / 2") },
            { Assertions.assertFalse(neEpsilon(1.0, 1.0), "ne actually ==") },
            { Assertions.assertTrue(neEpsilon(1.0, 1.0 + EPSILON * 2.0), "ne actually < by epsilon * 2 passes") },
            { Assertions.assertFalse(neEpsilon(1.0, 1.0 - EPSILON / 2.0), "ne actually > by epsilon / 2") },
            { Assertions.assertTrue(neEpsilon(1.0, 1.0 - EPSILON * 2.0), "ne actually > by epsilon * 2 passes") }
        )
    }
}