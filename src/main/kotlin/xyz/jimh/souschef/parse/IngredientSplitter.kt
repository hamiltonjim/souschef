/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.parse

/**
 * Class that parses lines, finding and counting ingredients.
 *
 * An ingredient consists of (perhaps) an amount, (perhaps) a unit,
 * and a food item.
 */
class IngredientSplitter {
    private val lineLists = mutableListOf<MutableList<String>>()

    fun read(line: String) {
        val tokens = line.split("[\\s(]+".toRegex())

        // find indices of tokens that are numbers
        val numberIndices = mutableListOf<Int>()
        for (ix in 1 until tokens.size) {   // skip first token
            try {
                NumberReader.parseNumber(tokens[ix])
                numberIndices.add(ix)
            } catch (_: NumberFormatException) {}
        }

        var tIndex = 0
        for ((index, _) in (0..numberIndices.size).withIndex()) {
            val stop = if (index == numberIndices.size)
                tokens.size
            else
                numberIndices[index]

            val sb = StringBuilder()
            for (tok in tIndex until stop) {
                if (sb.isNotEmpty()) sb.append(' ')
                sb.append(tokens[tok])
            }
            tIndex = stop
            if (lineLists.size <= index) {
                val list = mutableListOf<String>()
                lineLists.add(list)
            }
            val list = lineLists[index]
            list.add(sb.toString())
        }
    }

    fun getIngredients(): List<String> {
        val list = mutableListOf<String>()
        for (column in lineLists) {
            list.addAll(column)
        }
        return list
    }
}