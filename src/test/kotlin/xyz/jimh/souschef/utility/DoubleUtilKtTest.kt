/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.utility

import kotlin.math.PI
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DoubleUtilKtTest {

    private val epsilon = 1e-5

    @Test
    fun `rounding works correctly`() {
        Assertions.assertAll("rounding works",
            { Assertions.assertEquals(3.14, PI.round(2), epsilon) },
            { Assertions.assertEquals(130.0, 127.5.round(-1), epsilon) },
            { Assertions.assertEquals(12.0, 12.4.round(0), epsilon) }
        )

    }
}