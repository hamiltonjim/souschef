/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.display

import java.util.*

class HtmlBuilder {
    companion object {
        private const val HTML = "html"
        private const val BODY = "body"
        private const val BREAK = "br"
        private const val HEAD = "head"
        private const val TABLE = "table"
        private const val ROW = "tr"
        private const val CELL = "td"
        private const val HCELL = "th"
    }

    private val header = StringBuilder()

    private val body = StringBuilder()
    private val headerStack = Stack<String>()

    private val elementStack = Stack<String>()

    fun initialize(bodyAttributes: Map<String, String>): HtmlBuilder {
        addHeaderElement(HEAD)
        addBodyElement(BODY, bodyAttributes).addBodyText("\n")
        return this
    }

    fun addBodyText(text: String): HtmlBuilder {
        body.append(text)
        return this
    }

    fun addHeaderText(text: String): HtmlBuilder {
        header.append(text)
        return this
    }

    fun addBreak(): HtmlBuilder {
        addBodyElement(BREAK, closing = true)
        return this
    }

    fun addWhitespace(): HtmlBuilder {
        addBodyText(" ")
        return this
    }

    fun addHeaderWhitespace(): HtmlBuilder {
        addHeaderText("\n")
        return this
    }

    fun startTable(attributes: Map<String, String> = emptyMap()): HtmlBuilder {
        addBodyElement(TABLE, attributes)
        return this
    }

    fun startRow(attributes: Map<String, String> = emptyMap()): HtmlBuilder {
        addBodyElement(ROW, attributes)
        return this
    }

    fun startHeadingCell(attributes: Map<String, String> = emptyMap()): HtmlBuilder {
        addBodyElement(HCELL, attributes)
        return this
    }

    fun startCell(attributes: Map<String, String> = emptyMap()): HtmlBuilder {
        addBodyElement(CELL, attributes)
        return this
    }

    fun addBodyElement(
        tag: String,
        attributes: Map<String, String> = emptyMap(),
        closing: Boolean = false
    ): HtmlBuilder {
        addElement(tag, attributes, body, elementStack, closing)
        return this
    }

    fun addHeaderElement(
        tag: String,
        attributes: Map<String, String> = emptyMap(),
        closing: Boolean = false
    ): HtmlBuilder {
        addElement(tag, attributes, header, headerStack, closing)
        return this
    }

    fun closeBodyElement(): HtmlBuilder {
        closeElement(body, elementStack)
        return this
    }

    fun closeHeaderElement(): HtmlBuilder {
        closeElement(header, headerStack)
        return this
    }

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
}