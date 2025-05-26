package xyz.jimh.souschef.utility

import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
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
        assertAll(
            { assertTrue(geEpsilon(1.0, 1.0 + EPSILON / 2.0), "ge actually < by epsilon / 2") },
            { assertTrue(geEpsilon(1.0, 1.0), "ge actually ==") },
            { assertFalse(geEpsilon(1.0, 1.0 + EPSILON * 2.0), "ge actually < by epsilon * 2 passes") }
        )
    }

    @Test
    fun gtEpsilonTest() {
        assertAll(
            { assertTrue(gtEpsilon(1.0, 1.0 + EPSILON / 2.0), "gt actually < by epsilon / 2") },
            { assertTrue(gtEpsilon(1.0, 1.0), "gt actually ==") },
            { assertFalse(gtEpsilon(1.0, 1.0 + EPSILON * 2.0), "gt actually < by epsilon * 2 passes") }
        )
    }

    @Test
    fun ltEpsilonTest() {
        assertAll(
            { assertTrue(ltEpsilon(1.0, 1.0 - EPSILON / 2.0), "lt actually < by epsilon / 2") },
            { assertTrue(ltEpsilon(1.0, 1.0), "lt actually ==") },
            { assertFalse(ltEpsilon(1.0, 1.0 - EPSILON * 2.0), "lt actually < by epsilon * 2 passes") }
        )
    }

    @Test
    fun leEpsilonTest() {
        assertAll(
            { assertTrue(leEpsilon(1.0, 1.0 - EPSILON / 2.0), "le actually < by epsilon / 2") },
            { assertTrue(leEpsilon(1.0, 1.0), "le actually ==") },
            { assertFalse(leEpsilon(1.0, 1.0 - EPSILON * 2.0), "le actually < by epsilon * 2 passes") }
        )
    }

    @Test
    fun eqEpsilonTest() {
        assertAll(
            { assertTrue(eqEpsilon(1.0, 1.0 + EPSILON / 2.0), "eq actually < by epsilon / 2") },
            { assertTrue(eqEpsilon(1.0, 1.0), "le actually ==") },
            { assertFalse(eqEpsilon(1.0, 1.0 + EPSILON * 2.0), "eq actually < by epsilon * 2 passes") },
            { assertTrue(eqEpsilon(1.0, 1.0 - EPSILON / 2.0), "eq actually > by epsilon / 2") },
            { assertFalse(eqEpsilon(1.0, 1.0 - EPSILON * 2.0), "eq actually > by epsilon * 2 passes") }
        )
    }

    @Test
    fun neEpsilonTest() {
        assertAll(
            { assertFalse(neEpsilon(1.0, 1.0 + EPSILON / 2.0), "ne actually < by epsilon / 2") },
            { assertFalse(neEpsilon(1.0, 1.0), "ne actually ==") },
            { assertTrue(neEpsilon(1.0, 1.0 + EPSILON * 2.0), "ne actually < by epsilon * 2 passes") },
            { assertFalse(neEpsilon(1.0, 1.0 - EPSILON / 2.0), "ne actually > by epsilon / 2") },
            { assertTrue(neEpsilon(1.0, 1.0 - EPSILON * 2.0), "ne actually > by epsilon * 2 passes") }
        )
    }
}