package xyz.jimh.souschef.data

import kotlin.test.assertEquals
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
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
            Executable { assertTrue(someErrors.component1().contains("foo")) },
            Executable { assertFalse(noErrors.component1().contains("foo")) },
        )
    }

    @Test
    fun `test serialization`() {
        val jsonSomeErrors = Json.encodeToString(someErrors)
        val jsonNoErrors = Json.encodeToString(noErrors)
        assertAll(
            Executable { assertTrue(jsonSomeErrors.contains("foo")) },
            Executable { assertTrue(jsonNoErrors.contains("[]")) },
        )
    }

    @Test
    fun `test equals`() {
        val new = someErrors.copy()
        assertAll(
            Executable { assertEquals(someErrors, new) },
            Executable { assertNotEquals(someErrors, null) },
            Executable { assertNotEquals(someErrors, noErrors) },
        )
    }

    @Test
    fun `test hashCode`() {
        val new = someErrors.copy()
        val noNew = noErrors.copy()
        assertAll(
            Executable { assertEquals(someErrors.hashCode(), new.hashCode()) },
            Executable { assertNotEquals(someErrors.hashCode(), noNew.hashCode()) },
            Executable { assertNotEquals(someErrors.hashCode(), null.hashCode()) },
            Executable { assertNotEquals(noErrors.hashCode(), new.hashCode()) },
            Executable { assertEquals(noErrors.hashCode(), noNew.hashCode()) },
            Executable { assertNotEquals(noErrors.hashCode(), null.hashCode()) },
        )
    }

    @Test
    fun `test toString`() {
        val new = someErrors.copy()
        assertAll(
            Executable { assertEquals(someErrors.toString(), new.toString()) },
            Executable { assertNotEquals(someErrors.toString(), noErrors.toString()) },
        )
    }

    @Test
    fun `test copy`() {
        val new = someErrors.copy()
        val newer = someErrors.copy(errors = listOf("foo", "bar", "baz"))
        assertAll(
            Executable { assertEquals(someErrors, new) },
            Executable { assertNotEquals(someErrors, newer) },
            Executable { assertNotEquals(new, newer) },
        )
    }
}