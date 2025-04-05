/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.display

/**
 * Object that can read a resource file and return its contents as a String.
 * File contents are saved in a cache.
 */
object ResourceText {

    private var textMap = HashMap<String, String>()

    /**
     * Gets any file in the class path, using its [filename]
     */
    fun get(filename: String): String {
        load(filename)
        return textMap[filename] ?: ""
    }

    /**
     * Gets any file in the static directory, using its partial path in [filename]
     */
    fun getStatic(filename: String): String {
        load("static/$filename")
        return textMap["static/$filename"] ?: ""
    }

    private fun load(filename: String) {
        if (!textMap.containsKey(filename)) {
            val text = ResourceText::class.java.classLoader
                .getResourceAsStream(filename)
                ?.bufferedReader()
                ?.readText()
                ?: ""
            if (text.isNotEmpty()) {
                textMap[filename] = text
            }
        }
    }

    /**
     * Clears the file cache.
     */
    fun flush() {
        textMap.clear()
    }
}