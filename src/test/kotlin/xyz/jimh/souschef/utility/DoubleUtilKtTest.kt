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
        assertAll("rounding works",
            { assertEquals(3.14, PI.round(2), epsilon) },
            { assertEquals(130.0, 127.5.round(-1), epsilon) },
            { assertEquals(12.0, 12.4.round(0), epsilon) }
        )

    }
}