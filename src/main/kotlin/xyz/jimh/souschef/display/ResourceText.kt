// Copyright © 2025 Jim Hamilton. All rights reserved.

package xyz.jimh.souschef.display

object ResourceText {

    private var textMap = HashMap<String, String>()

    fun get(filename: String): String {
        load(filename)
        return textMap[filename] ?: ""
    }

    private fun load(filename: String) {
        if (!textMap.containsKey(filename)) {
            val text = ResourceText::class.java.classLoader
                .getResourceAsStream("static/$filename")?.bufferedReader()?.readText() ?: ""
            if (text.isNotEmpty()) {
                textMap[filename] = text
            }
        }
    }
}