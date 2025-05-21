/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.parse

import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.config.SpringContext
import xyz.jimh.souschef.data.AUnit
import xyz.jimh.souschef.data.UnitDao

class IngredientParser(aLine: String) {
    private val tokens = aLine.split(' ', '\t',)
    private var amountTokens: Int = -1
    private var unitTokens: Int = -1
    private var itemTokens: Int = -1

    var amount: Double = 0.0
    var unit: AUnit? = null
    lateinit var item: String

    private val nonIngredientKeywords = Preferences.getLanguageArray("nonIngredientKeywords")

    fun findAmount(): Double {
        val amountBuilder = StringBuilder()
        amountTokens = 0
        for (token in tokens) {
            if (NumberReader.isNumber(token)) {
                if (amountBuilder.isNotEmpty()) amountBuilder.append(' ')
                amountBuilder.append(token)
                amountTokens++
            } else break
            if (amountTokens >= 2) break
        }
        amount = NumberReader.parseNumber(amountBuilder.toString())
        return amount
    }

    fun findUnit(): AUnit? {
        if (amountTokens < 0) findAmount()
        unitTokens = 0
        val unitBuilder = StringBuilder()
        for (tokenIndex in amountTokens until tokens.size) {
            if (unitBuilder.isNotEmpty()) unitBuilder.append(' ')
            unitBuilder.append(tokens[tokenIndex])
            val aUnit = isUnit(unitBuilder.toString())
            if (aUnit != null) {
                unitTokens = tokenIndex - amountTokens + 1
                unit = aUnit
                return unit
            }
        }
        return null
    }

    private fun isUnit(unit: String): AUnit? {
        loadUnitController()
        return matchesUnit(unit)
    }

    fun findIngredient(): String {
        if (unitTokens < 0) findUnit()
        val nextIngredientToken = findSplit()
        val totalSize = if (nextIngredientToken < 0)
            tokens.size
        else
            nextIngredientToken
        itemTokens = totalSize - amountTokens - unitTokens
        val ingredientBuilder = StringBuilder()
        for (tokenIndex in unitTokens + amountTokens until tokens.size) {
            if (ingredientBuilder.isNotEmpty()) ingredientBuilder.append(' ')
            ingredientBuilder.append(tokens[tokenIndex])
        }
        item = ingredientBuilder.toString()
        return item
    }

    internal fun findSplit(): Int {
        val size = tokens.size
        for (tokenIndex in amountTokens + unitTokens until size - 1) {
            if (!isNumber(tokens[tokenIndex])) {
                continue
            }
            val unitIndex = if (isNumber(tokens[tokenIndex + 1])) {
                tokenIndex + 2
            } else
                tokenIndex + 1
            if (unitIndex < size - 1)
                return tokenIndex
            else if (isUnit(tokens[unitIndex]) !is AUnit)
                return tokenIndex
        }

        return -1
    }

    private fun isNumber(token: String): Boolean {
        try {
            NumberReader.parseNumber(token)
            return true
        } catch (_: NumberFormatException) {
            return false
        }
    }

    fun isIngredient(): Boolean {
        if (tokens.containsAny(nonIngredientKeywords))
            return false
        if (itemTokens < 0) findIngredient()
        return itemTokens < 10
    }

    companion object {
        internal lateinit var unitDao: UnitDao

        internal var units: MutableList<AUnit> = mutableListOf()
        internal fun loadUnitController() {
            if (!this::unitDao.isInitialized) {
                unitDao = SpringContext.getBean(UnitDao::class.java)
            }
            if (units.isEmpty()) {
                units.addAll(unitDao.findAll())
            }
        }

        internal fun matchesUnit(unit: String): AUnit? {
            if (unit.isBlank()) return null
            for (aUnit in units) {
                if (unit.equals(aUnit.name, ignoreCase = true))
                    return aUnit
                if (unit.equals(aUnit.abbrev, ignoreCase = true))
                    return aUnit

                val singular = unit.substringBeforeLast('s')
                if (singular.equals(aUnit.name, ignoreCase = true))
                    return aUnit

                // edge case: don't match "tsp" and "T", as they are very different!
                if (!unit.startsWith("tsp")) {
                    if (compareVariants(unit, aUnit, 's'))
                        return aUnit
                }
                if (compareVariants(unit, aUnit, '.'))
                    return aUnit
            }
            return null
        }

        private fun compareVariants(unit: String, aUnit: AUnit, delimiter: Char): Boolean {
            val stripDot = unit.substringBeforeLast(delimiter)
            if (stripDot.equals(aUnit.abbrev, ignoreCase = true))
                return true
            if ("$stripDot.".equals(aUnit.abbrev, ignoreCase = true))
                return true
            if (stripDot.equals(aUnit.altAbbrev, ignoreCase = true))
                return true
            if ("$stripDot.".equals(aUnit.altAbbrev, ignoreCase = true))
                return true
            return false
        }
    }
}

fun List<String>.containsAny(list: List<String>): Boolean {
    for (item in this) {
        for (other in list) {
            if (item.equals(other, ignoreCase = true))
                return true
        }
    }

    return false
}