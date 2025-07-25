/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

import java.io.BufferedReader
import java.io.InputStreamReader
import org.springframework.core.io.Resource

/**
 * Object that loads translated strings into a map. The key in the map
 * is a reference string, used to find the translated string.
 * @property strings map of keys and localized strings
 * @property arrays map of keys and arrays of localized strings
 */
class StringsFileLoader {
    val strings = mutableMapOf<String, String>()
    val arrays = mutableMapOf<String, List<String>>()

    private var currentKey: String? = null
    private var currentList = mutableListOf<String>()

    /**
     * Loads strings from [rez]. If [limit] is greater than zero, only
     * loads the first [limit] strings. If [limit] is zero or negative,
     * loads all strings from the file.
     *
     * A blank line, or a line that begins with '#', is ignored. This
     * eases formatting and allows comments.
     */
    fun load(rez: Resource, limit: Int = 0): StringsFileLoader {
        val stream = rez.inputStream
        stream.use { stream ->
            val reader = BufferedReader(InputStreamReader(stream))
            if (limit > 0) {
                reader.use {
                    var ctr = 0
                    while (ctr++ < limit) {
                        val line = it.readLine() ?: break
                        parseLine(line, strings)
                    }
                }
            } else
                reader.useLines { lines -> lines.forEach { parseLine(it, strings) } }
        }
        return this
    }

    private fun parseLine(inLine: String, map: MutableMap<String, String>) {
        val line = inLine.substringBefore("#")  // ignore comments
        if (line.isEmpty()) return

        val key = currentKey
        if (key == null) {
            val parts = line.split('=', limit = 2)
            if (parts.size == 2) {
                if (parts[1] == "[") {
                    currentKey = parts[0]
                    currentList.clear()
                } else {
                    map[parts[0]] = parts[1]
                }
            }
        } else {
            if (line == "]") {
                arrays[key] = currentList.toList()
                currentKey = null
                currentList.clear()
            } else {
                currentList += line.substringAfter("-")
            }
        }
    }
}