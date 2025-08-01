package xyz.jimh.souschef.info

import kotlin.test.assertEquals
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.actuate.info.Info

class HostInfoTest {

    @Test
    fun `test contribute`() {
        val builder = Info.Builder()
        val randomizer = RandomUtils.insecure()
        val stringRandomizer = RandomStringUtils.insecure()

        val hostInfo = HostInfo()
        hostInfo.host = stringRandomizer.nextAlphanumeric(10, 50)
        hostInfo.port = randomizer.randomInt(1024, 65535)
        hostInfo.contextPath = stringRandomizer.nextAlphanumeric(10)

        hostInfo.contribute(builder)
        val info = builder.build()
        val map = info.get("hostInfo") as Map<*, *>

        assertAll(
            { assertEquals(hostInfo.host, map["host"]) },
            { assertEquals(hostInfo.port, map["port"]) },
            { assertEquals(hostInfo.contextPath, map["contextPath"]) },
        )
    }

    @Test
    fun `test null host`() {
        val builder = Info.Builder()
        val randomizer = RandomUtils.insecure()
        val stringRandomizer = RandomStringUtils.insecure()

        val hostInfo = HostInfo()
        hostInfo.port = randomizer.randomInt(1024, 65535)
        hostInfo.contextPath = stringRandomizer.nextAlphanumeric(10)

        hostInfo.contribute(builder)
        val info = builder.build()
        val map = info.get("hostInfo") as Map<*, *>

        assertAll(
            { assertEquals(hostInfo.host, map["host"]) },
            { assertEquals(hostInfo.port, map["port"]) },
            { assertEquals(hostInfo.contextPath, map["contextPath"]) },
            { assertEquals(hostInfo.protocol, map["protocol"]) },
        )
    }

    @Test
    fun `test null builder`() {
        val hostInfo = HostInfo()
        assertThrows<IllegalArgumentException> { hostInfo.contribute(null) }
    }
}