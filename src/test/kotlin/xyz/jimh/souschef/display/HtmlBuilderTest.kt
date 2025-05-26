package xyz.jimh.souschef.display

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class HtmlBuilderTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun initialize_test() {
        val htmlBuilder = htmlBuilder()

        assertAll(
            { assertEquals(HtmlBuilder.HEAD, htmlBuilder.headerStack.peek(), "Top header element is not HEAD") },
            { assertEquals(HtmlBuilder.BODY, htmlBuilder.elementStack.peek(), "Top body element is not BODY") },
        )
    }

    private fun htmlBuilder(): HtmlBuilder {
        val htmlBuilder = HtmlBuilder()
        htmlBuilder.initialize(emptyMap())
        return htmlBuilder
    }

    @Test
    fun addBodyText_test() {
        val htmlBuilder = htmlBuilder()

        htmlBuilder.addBodyText("Hello World")
        assertTrue(htmlBuilder.body.contains("Hello World"), "body text was missing")
    }

    @Test
    fun addHeaderText_test() {
        val htmlBuilder = htmlBuilder()

        htmlBuilder.addHeaderText("Hello World")
        assertTrue(htmlBuilder.header.contains("Hello World"), "header text was missing")
    }

    @Test
    fun addBreak_test() {
        val htmlBuilder = htmlBuilder()

        htmlBuilder.addBreak()
        assertTrue(htmlBuilder.body.endsWith("<br/>"), "break was not final element")
    }

    @Test
    fun addWhitespace_test() {
        val htmlBuilder = htmlBuilder()

        htmlBuilder.addWhitespace()
        assertTrue(htmlBuilder.body.endsWith(" "), "whitespace was not final element")
    }

    @Test
    fun addHeaderWhitespace_test() {
        val htmlBuilder = htmlBuilder()

        htmlBuilder.addHeaderWhitespace()
        assertTrue(htmlBuilder.header.endsWith("\n"), "whitespace was not final element")
    }

    @Test
    fun startTable_test() {
        val htmlBuilder = htmlBuilder()

        htmlBuilder.startTable()
        assertAll(
            { assertTrue(htmlBuilder.body.contains("<table>"), "No table in html") },
            { assertEquals(HtmlBuilder.TABLE, htmlBuilder.elementStack.peek(), "table was not last element") },
        )
    }

    @Test
    fun startRow_test() {
        val htmlBuilder = htmlBuilder()

        htmlBuilder.startTable()
        htmlBuilder.startRow()
        assertAll(
            { assertTrue(htmlBuilder.body.contains("<tr>"), "No tr in html") },
            { assertTrue(htmlBuilder.body.contains("<table>"), "No table in html") },
            { assertEquals(HtmlBuilder.ROW, htmlBuilder.elementStack.peek(), "row is not last element") },
        )
    }

    @Test
    fun startHeadingCell_test() {
        val htmlBuilder = htmlBuilder()

        htmlBuilder.startTable()
        htmlBuilder.startRow()
        htmlBuilder.startHeadingCell()
        assertAll(
            { assertTrue(htmlBuilder.elementStack.contains(HtmlBuilder.ROW)) },
            { assertTrue(htmlBuilder.elementStack.contains(HtmlBuilder.TABLE)) },
            { assertEquals(HtmlBuilder.HCELL, htmlBuilder.elementStack.peek(), "cell is not last element") },
        )
    }

    @Test
    fun startCell_test() {
        val htmlBuilder = htmlBuilder()

        htmlBuilder.startTable()
        htmlBuilder.startRow()
        htmlBuilder.startCell()
        assertAll(
            { assertTrue(htmlBuilder.elementStack.contains(HtmlBuilder.ROW)) },
            { assertTrue(htmlBuilder.elementStack.contains(HtmlBuilder.TABLE)) },
            { assertEquals(HtmlBuilder.CELL, htmlBuilder.elementStack.peek(), "cell is not last element") },
        )
    }

    @Test
    fun addBodyElement_test() {
        val htmlBuilder = htmlBuilder()

        htmlBuilder.addBodyElement("div")
        assertEquals("div", htmlBuilder.elementStack.peek(), "div not most recent element")
        htmlBuilder.addBodyElement("input")
        assertEquals("input", htmlBuilder.elementStack.peek(), "input not most recent element")
        htmlBuilder.closeBodyElement()
        assertEquals("div", htmlBuilder.elementStack.peek(), "div not most recent element")
        htmlBuilder.closeBodyElement()
        assertEquals(HtmlBuilder.BODY, htmlBuilder.elementStack.peek(), "body not most recent element")
    }

    @Test
    fun addHeaderElement_test() {
        val htmlBuilder = htmlBuilder()

        htmlBuilder.addHeaderElement("style")
        assertEquals("style", htmlBuilder.headerStack.peek(), "style not most recent element")
        htmlBuilder.closeHeaderElement()
        assertEquals(HtmlBuilder.HEAD, htmlBuilder.headerStack.peek(), "head not most recent element")
    }
}