package xyz.jimh.souschef.info

import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
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
}