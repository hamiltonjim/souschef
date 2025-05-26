package xyz.jimh.souschef.display

import java.io.FileNotFoundException
import java.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import xyz.jimh.souschef.config.Broadcaster
import xyz.jimh.souschef.config.Preferences

class ResourceTextTest {

    private lateinit var resourceText: ResourceText

    @BeforeEach
    fun setUp() {
        resourceText = ResourceText
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun `check that listener listens`() {
        resourceText.lastMessageTime = null
        resourceText.lastMessage = null
        `getStatic succeeds`()
        assertFalse(resourceText.textMap.isEmpty())
        resourceText.init()
        var messageTime = resourceText.lastMessageTime
        assertNull(messageTime)
        Preferences.broadcast("bar", "foo")
        messageTime = resourceText.lastMessageTime
        assertNotNull(messageTime)
        assertEquals("foo" to "bar", resourceText.lastMessage)
        Preferences.broadcast("baz")
        var rlTime = resourceText.lastMessageTime ?: Instant.EPOCH
        assertTrue(rlTime >= messageTime)
        assertEquals(Broadcaster.NO_NAME to "baz", resourceText.lastMessage)
        Preferences.broadcast("es_US", "locale")
        rlTime = resourceText.lastMessageTime ?: Instant.EPOCH
        assertTrue(rlTime >= messageTime)
        assertTrue(resourceText.textMap.isEmpty())
        assertEquals("locale" to "es_US", resourceText.lastMessage)
        resourceText.destroy()
    }

    @Test
    fun `getStatic succeeds`() {
        resourceText.flush()
        val javaScript = resourceText.getStatic("fauxAlert.js")
        val js2 = resourceText.getStatic("fauxAlert.js")
        assertAll(
            { assertTrue(javaScript.isNotEmpty()) },
            { assertEquals(javaScript, js2) },
            { assertNotNull(resourceText.textMap["static/fauxAlert.js"]) }
        )
    }

    @Test
    fun `getStatic fails`() {
        val notFound = resourceText.getStatic("not_found.txt")
        assertAll(
            { assertTrue(notFound.isEmpty()) },
            { assertTrue(resourceText.textMap["not_found.txt"].isNullOrEmpty()) }
        )
    }

    @Test
    fun `get succeeds`() {
        resourceText.flush()
        val javaScript = resourceText.get("static/fauxAlert.js")
        val js2 = resourceText.get("static/fauxAlert.js")
        assertAll(
            { assertTrue(javaScript.isNotEmpty()) },
            { assertEquals(javaScript, js2) },
        )
    }

    @Test
    fun `gets fail`() {
        val notFound = resourceText.getStatic("not_found.txt")
        val nf2 = resourceText.getStatic("not_found.txt")
        val nf3 = resourceText.get("not_found.txt")
        assertAll(
            { assertTrue(notFound.isEmpty()) },
            { assertTrue(nf2.isEmpty()) },
            { assertTrue(nf3.isEmpty()) },
        )
    }

    @Test
    fun `get base64 succeeds`() {
        val prediction = "IyBubyBsb2NhbGUsIGZvciB0ZXN0aW5nIHRoYXQgZWRnZSBjYXNlCmxvY2FsZQpsYW5ndWFnZT1LbGluZ29u"
        val b64 = resourceText.getBase64("static/nolocale/strings")
        assertEquals(prediction, b64)
    }

    @Test
    fun `get base64 fails`() {
        assertThrows<FileNotFoundException> { resourceText.getBase64("not_found.txt") }
    }
}