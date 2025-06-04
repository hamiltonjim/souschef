/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.utility

import kotlin.math.PI
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class DoubleUtilKtTest {

    private val epsilon = 1e-5

    @Test
    fun `rounding works correctly`() {
        assertAll(
            "rounding works",
            { assertEquals(3.14, PI.round(2), epsilon) },
            { assertEquals(130.0, 127.5.round(-1), epsilon) },
            { assertEquals(12.0, 12.4.round(0), epsilon) },
            { assertEquals(12000.0, 12443.0.round(-3), epsilon) },
            { assertEquals(12400.0, 12443.0.round(-2), epsilon) },
            { assertEquals(3.142, PI.round(3), epsilon) },
            { assertEquals(3.1, PI.round(1), epsilon) },
            { assertEquals(3.14159, PI.round(5), epsilon) },
            // the speed of light to 4 significant digits
            { assertEquals(300_000_000.0, 299_792_458.0.round(-6), epsilon) },
        )

    }
}