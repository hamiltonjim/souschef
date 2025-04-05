/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.display

import java.util.*

/**
 * Class that builds an HTML document. The header and body are built
 * separately, and merged at the end; the result is returned as a
 * String.
 */
class HtmlBuilder {
    // items are internal so test class can see them
    internal val header = StringBuilder()
    internal val body = StringBuilder()
    internal val headerStack = Stack<String>()
    internal val elementStack = Stack<String>()

    /**
     * Begins creation of a screen.
     * @return this
     */
    fun initialize(bodyAttributes: Map<String, String>): HtmlBuilder {
        addHeaderElement(HEAD)
        addBodyElement(BODY, bodyAttributes).addBodyText("\n")
        return this
    }

    /**
     * As the name implies, just adds arbitrary [text] to the body of the HTML.
     */
    fun addBodyText(text: String): HtmlBuilder {
        body.append(text)
        return this
    }

    /**
     * Adds arbitrary [text] to the header.
     */
    fun addHeaderText(text: String): HtmlBuilder {
        header.append(text)
        return this
    }

    /**
     * Adds a break element "&lt;&#65279;br/&gt;" to the body.
     */
    fun addBreak(): HtmlBuilder {
        addBodyElement(BREAK, closing = true)
        return this
    }

    /**
     * Just adds a space (\u0020) to the body.
     */
    fun addWhitespace(): HtmlBuilder {
        addBodyText(" ")
        return this
    }

    /**
     * Just adds a newline to the header.
     */
    fun addHeaderWhitespace(): HtmlBuilder {
        addHeaderText("\n")
        return this
    }

    /**
     * Starts a table "&lt;&#65279;table&gt;" with the given [attributes].
     */
    fun startTable(attributes: Map<String, String> = emptyMap()): HtmlBuilder {
        addBodyElement(TABLE, attributes)
        return this
    }

    /**
     * Starts a table row "&lt;&#65279;tr&gt;" with the given [attributes].
     */
    fun startRow(attributes: Map<String, String> = emptyMap()): HtmlBuilder {
        addBodyElement(ROW, attributes)
        return this
    }

    /**
     * Starts a table header cell "&lt;&#65279;th&gt;" with the given [attributes].
     */
    fun startHeadingCell(attributes: Map<String, String> = emptyMap()): HtmlBuilder {
        addBodyElement(HCELL, attributes)
        return this
    }

    /**
     * Starts a table cell "&lt;&#65279;td&gt;" with the given [attributes].
     */
    fun startCell(attributes: Map<String, String> = emptyMap()): HtmlBuilder {
        addBodyElement(CELL, attributes)
        return this
    }

    /**
     * Starts an arbitrary element [tag] with the given [attributes]. If [closing]
     * is true, also closes the element.
     */
    fun addBodyElement(
        tag: String,
        attributes: Map<String, String> = emptyMap(),
        closing: Boolean = false
    ): HtmlBuilder {
        addElement(tag, attributes, body, elementStack, closing)
        return this
    }

    /**
     * Starts an arbitrary element [tag] with the given [attributes] to the header.
     * If [closing] is true, also closes the element.
     */
    fun addHeaderElement(
        tag: String,
        attributes: Map<String, String> = emptyMap(),
        closing: Boolean = false
    ): HtmlBuilder {
        addElement(tag, attributes, header, headerStack, closing)
        return this
    }

    /**
     * Closes the most recently opened body element (popping it from a stack).
     */
    fun closeBodyElement(): HtmlBuilder {
        closeElement(body, elementStack)
        return this
    }

    /**
     * Closes the most recently opened header element (popping it from a stack).
     */
    fun closeHeaderElement(): HtmlBuilder {
        closeElement(header, headerStack)
        return this
    }

    /**
     * Closes all open elements in the header and the body. Creates an
     * &lt;&#65279;html&gt; element with the given [attributes], adds both
     * the header and the body to that element, and closes it. The
     * resulting HTML is then returned as a String.
     */
    fun get(attributes: Map<String, String> = emptyMap()): String {
        val html = StringBuilder()
        val htmlStack = Stack<String>()
        addElement(HTML, attributes, html, htmlStack)

        closeHeader()
        html.append(header.toString()).append('\n')

        closeBody()
        html.append(body.toString()).append('\n')

        closeElement(html, htmlStack)
        return html.toString()
    }

    private fun closeBody() {
        closeAll(body, elementStack)
    }

    private fun closeHeader() {
        closeAll(header, headerStack)
    }

    private fun addElement(
        element: String,
        attributes: Map<String, String>,
        builder: StringBuilder,
        stack: Stack<String>,
        closing: Boolean = false
    ) {
        if (!closing) {
            stack.push(element)
        }
        builder.append('<').append(element)
        attributes.forEach { builder.append(' ').append(it.key).append("=\"").append(it.value).append('"') }
        if (closing) {
            builder.append('/')
        }
        builder.append('>')
    }

    private fun closeElement(builder: StringBuilder, stack: Stack<String>) {
        val element = stack.pop()
        builder.append("</").append(element).append('>')
    }

    private fun closeAll(builder: StringBuilder, stack: Stack<String>) {
        while (!stack.empty()) {
            closeElement(builder, stack)
        }
    }

    /**
     * Common tags.
     */
    companion object {
        internal const val HTML = "html"
        internal const val BODY = "body"
        internal const val BREAK = "br"
        internal const val HEAD = "head"
        internal const val TABLE = "table"
        internal const val ROW = "tr"
        internal const val CELL = "td"
        internal const val HCELL = "th"
    }
}
