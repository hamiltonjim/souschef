package xyz.jimh.souschef.config

import kotlin.math.PI
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

class DoubleUtilKtTest {

    private val epsilon = 1e-5

    @Test
    fun `rounding works correctly`() {
        Assertions.assertAll("rounding works",
            Executable { Assertions.assertEquals(3.14, PI.round(2), epsilon) },
            Executable { Assertions.assertEquals(130.0, 127.5.round(-1), epsilon) },
            Executable { Assertions.assertEquals(12.0, 12.4.round(0), epsilon) }
        )

    }
}