package xyz.jimh.souschef.info

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.actuate.info.Info

class UpTimeInfoTest {
    @Test
    fun `normal run`() {
        val builder = Info.Builder()
        val contributor = UpTimeInfo()
        contributor.contribute(builder)
        val info = builder.build().details
        assertAll(
            { assertEquals(2, info.size) },
            { assertEquals(setOf("startTime", "upTime"), info.keys) },
        )
    }

    @Test
    fun `null builder`() {
        val contributor = UpTimeInfo()
        assertThrows<IllegalArgumentException> { contributor.contribute(null) }
    }
}