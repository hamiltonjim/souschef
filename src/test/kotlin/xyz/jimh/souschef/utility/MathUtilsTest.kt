package xyz.jimh.souschef.utility

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
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
            Executable { Assertions.assertTrue(geEpsilon(1.0, 1.0 + EPSILON / 2.0), "ge actually < by epsilon / 2") },
            Executable { Assertions.assertTrue(geEpsilon(1.0, 1.0), "ge actually ==") },
            Executable { Assertions.assertFalse(geEpsilon(1.0, 1.0 + EPSILON * 2.0), "ge actually < by epsilon * 2 passes") }
        )
    }

    @Test
    fun gtEpsilonTest() {
        Assertions.assertAll(
            Executable { Assertions.assertTrue(gtEpsilon(1.0, 1.0 + EPSILON / 2.0), "gt actually < by epsilon / 2") },
            Executable { Assertions.assertTrue(gtEpsilon(1.0, 1.0), "gt actually ==") },
            Executable { Assertions.assertFalse(gtEpsilon(1.0, 1.0 + EPSILON * 2.0), "gt actually < by epsilon * 2 passes") }
        )
    }

    @Test
    fun ltEpsilonTest() {
        Assertions.assertAll(
            Executable { Assertions.assertTrue(ltEpsilon(1.0, 1.0 - EPSILON / 2.0), "lt actually < by epsilon / 2") },
            Executable { Assertions.assertTrue(ltEpsilon(1.0, 1.0), "lt actually ==") },
            Executable { Assertions.assertFalse(ltEpsilon(1.0, 1.0 - EPSILON * 2.0), "lt actually < by epsilon * 2 passes") }
        )
    }

    @Test
    fun leEpsilonTest() {
        Assertions.assertAll(
            Executable { Assertions.assertTrue(leEpsilon(1.0, 1.0 - EPSILON / 2.0), "le actually < by epsilon / 2") },
            Executable { Assertions.assertTrue(leEpsilon(1.0, 1.0), "le actually ==") },
            Executable { Assertions.assertFalse(leEpsilon(1.0, 1.0 - EPSILON * 2.0), "le actually < by epsilon * 2 passes") }
        )
    }

    @Test
    fun eqEpsilonTest() {
        Assertions.assertAll(
            Executable { Assertions.assertTrue(eqEpsilon(1.0, 1.0 + EPSILON / 2.0), "eq actually < by epsilon / 2") },
            Executable { Assertions.assertTrue(eqEpsilon(1.0, 1.0), "le actually ==") },
            Executable { Assertions.assertFalse(eqEpsilon(1.0, 1.0 + EPSILON * 2.0), "eq actually < by epsilon * 2 passes") },
            Executable { Assertions.assertTrue(eqEpsilon(1.0, 1.0 - EPSILON / 2.0), "eq actually > by epsilon / 2") },
            Executable { Assertions.assertFalse(eqEpsilon(1.0, 1.0 - EPSILON * 2.0), "eq actually > by epsilon * 2 passes") }
        )
    }

    @Test
    fun neEpsilonTest() {
        Assertions.assertAll(
            Executable { Assertions.assertFalse(neEpsilon(1.0, 1.0 + EPSILON / 2.0), "ne actually < by epsilon / 2") },
            Executable { Assertions.assertFalse(neEpsilon(1.0, 1.0), "ne actually ==") },
            Executable { Assertions.assertTrue(neEpsilon(1.0, 1.0 + EPSILON * 2.0), "ne actually < by epsilon * 2 passes") },
            Executable { Assertions.assertFalse(neEpsilon(1.0, 1.0 - EPSILON / 2.0), "ne actually > by epsilon / 2") },
            Executable { Assertions.assertTrue(neEpsilon(1.0, 1.0 - EPSILON * 2.0), "ne actually > by epsilon * 2 passes") }
        )
    }
}