/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

import java.io.File
import java.nio.file.Files

object StringsFileLoader {
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
        }
        file.forEachLine { parseLine(it, map) }

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