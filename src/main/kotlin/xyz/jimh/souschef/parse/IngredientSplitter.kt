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
 *
 * Call [read] for each line that is presumed to hold ingredients, then
 * call [getIngredients] to retrieve the whole list.
 */
class IngredientSplitter {
    private val lineLists = mutableListOf<MutableList<String>>()

    /**
     * Given a [line] of text, will determine whether there are multiple ingredient strings
     * in the line. Ingredients are split into lists, so that the order of ingredients
     * is preserved, assuming they start in the leftmost column, going down, and proceeding
     * to columns further to the right.
     *
     * After all ingredient lines are read, call [getIngredients] to retrieve the list pf
     * ingredients in order.
     */
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
        for (index in 0..numberIndices.size) {
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

    /**
     * Once all ingredient lines have been [read], this function will return a list of
     * ingredient strings in the assumed order (top to bottom, then left to right).
     */
    fun getIngredients(): List<String> {
        val list = mutableListOf<String>()
        for (column in lineLists) {
            list.addAll(column)
        }
        return list
    }
}