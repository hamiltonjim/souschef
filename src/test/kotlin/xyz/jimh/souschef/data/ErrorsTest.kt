package xyz.jimh.souschef.data

import kotlin.test.assertEquals
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ErrorsTest {

    private val noErrors = Errors(emptyList())
    private val someErrors = Errors(listOf("foo"))

    @Test
    fun isEmpty() {
        assertAll(
            { assertTrue(noErrors.isEmpty()) },
            { assertFalse(someErrors.isEmpty()) }
        )
    }

    @Test
    fun isNotEmpty() {
        assertAll(
            { assertFalse(noErrors.isNotEmpty()) },
            { assertTrue(someErrors.isNotEmpty()) },
        )
    }

    @Test
    fun size() {
        assertAll(
            { assertEquals(0, noErrors.size) },
            { assertEquals(1, someErrors.size) },
        )
    }

    @Test
    fun getErrors() {
        assertAll(
            { assertTrue(someErrors.errors.contains("foo")) },
            { assertFalse(noErrors.errors.contains("foo")) },
            { assertTrue(someErrors.component1().contains("foo")) },
            { assertFalse(noErrors.component1().contains("foo")) },
        )
    }

    @Test
    fun `test serialization`() {
        val jsonSomeErrors: String = Json.encodeToString(someErrors)
        val jsonNoErrors: String = Json.encodeToString(noErrors)
        assertAll(
            { assertTrue(jsonSomeErrors.contains("foo")) },
            { assertTrue(jsonNoErrors.contains("[]")) },
        )
    }

    @Test
    fun `test equals`() {
        val new = someErrors.copy()
        assertAll(
            { assertEquals(someErrors, new) },
            { assertNotEquals(someErrors, null) },
            { assertNotEquals(someErrors, noErrors) },
        )
    }

    @Test
    fun `test hashCode`() {
        val new = someErrors.copy()
        val noNew = noErrors.copy()
        assertAll(
            { assertEquals(someErrors.hashCode(), new.hashCode()) },
            { assertNotEquals(someErrors.hashCode(), noNew.hashCode()) },
            { assertNotEquals(someErrors.hashCode(), null.hashCode()) },
            { assertNotEquals(noErrors.hashCode(), new.hashCode()) },
            { assertEquals(noErrors.hashCode(), noNew.hashCode()) },
            { assertNotEquals(noErrors.hashCode(), null.hashCode()) },
        )
    }

    @Test
    fun `test toString`() {
        val new = someErrors.copy()
        assertAll(
            { assertEquals(someErrors.toString(), new.toString()) },
            { assertNotEquals(someErrors.toString(), noErrors.toString()) },
        )
    }

    @Test
    fun `test copy`() {
        val new = someErrors.copy()
        val newer = someErrors.copy(errors = listOf("foo", "bar", "baz"))
        assertAll(
            { assertEquals(someErrors, new) },
            { assertNotEquals(someErrors, newer) },
            { assertNotEquals(new, newer) },
        )
    }
}