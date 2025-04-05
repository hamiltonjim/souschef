package xyz.jimh.souschef.display

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

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
    fun `getStatic succeeds`() {
        resourceText.flush()
        val javaScript = resourceText.getStatic("fauxAlert.js")
        assertAll(
            Executable { assertNotNull(javaScript) },
            Executable { assertTrue(javaScript.isNotEmpty()) }
        )
    }

    @Test
    fun `getStatic fails`() {
        val notFound = resourceText.getStatic("not_found.txt")
        assertAll(
            Executable { assertNotNull(notFound) },
            Executable { assertTrue(notFound.isEmpty()) }
        )
    }

    @Test
    fun `get succeeds`() {
        resourceText.flush()
        val javaScript = resourceText.get("static/fauxAlert.js")
        assertAll(
            Executable { assertNotNull(javaScript) },
            Executable { assertTrue(javaScript.isNotEmpty()) }
        )
    }

    @Test
    fun `get fails`() {
        val notFound = resourceText.getStatic("not_found.txt")
        assertAll(
            Executable { assertNotNull(notFound) },
            Executable { assertTrue(notFound.isEmpty()) }
        )
    }
}