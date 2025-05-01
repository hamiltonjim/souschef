/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

import java.io.File
import java.nio.file.Files

/**
 * Object that loads translated strings into a map. The key in the map
 * is a reference string, used to find the translated string.
 */
object StringsFileLoader {
    /**
     * Loads strings from [file]. If [limit] is greater than zero, only
     * loads the first [limit] strings. If [limit] is zero or negative,
     * loads all strings from the file.
     *
     * A blank line, or a line that begins with '#', is ignored. This
     * eases formatting and allows comments.
     */
    fun load(file: File, limit: Int = 0): Map<String, String> {
        val map = mutableMapOf<String,String>()
        if (limit > 0) {
            Files.newBufferedReader(file.toPath()).use { reader ->
                var ctr = 0
                while (ctr++ < limit) {
                    val line = reader.readLine() ?: break
                    parseLine(line, map)
                }
            }
        } else {
            file.forEachLine { parseLine(it, map) }
        }

        return map
    }

    private fun parseLine(line: String, map: MutableMap<String, String>) {
        if (line.isEmpty()) return
        if (line[0] == '#') return  // skip comments

        val parts = line.split('=', limit = 2)
        if (parts.size == 2) {
            map[parts[0]] = parts[1]
        }
    }
}