package xyz.jimh.souschef.data

import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

class ErrorsTest {

    private val noErrors = Errors(emptyList())
    private val someErrors = Errors(listOf("foo"))

    @Test
    fun isEmpty() {
        assertAll(
            Executable { assertTrue(noErrors.isEmpty()) },
            Executable { assertFalse(someErrors.isEmpty()) }
        )
    }

    @Test
    fun isNotEmpty() {
        assertAll(
            Executable { assertFalse(noErrors.isNotEmpty()) },
            Executable { assertTrue(someErrors.isNotEmpty()) },
        )
    }

    @Test
    fun size() {
        assertAll(
            Executable { assertEquals(0, noErrors.size) },
            Executable { assertEquals(1, someErrors.size) },
        )
    }

    @Test
    fun getErrors() {
        assertAll(
            Executable { assertTrue(someErrors.errors.contains("foo")) },
            Executable { assertFalse(noErrors.errors.contains("foo")) },
        )
    }
}