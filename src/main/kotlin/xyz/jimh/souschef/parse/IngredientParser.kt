/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.parse

import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.config.SpringContext
import xyz.jimh.souschef.data.AUnit
import xyz.jimh.souschef.data.UnitDao
import xyz.jimh.souschef.utility.containsAny

/**
 * Class that will try to find an ingredient in a recipe. An ingredient consists of an
 * optional [amount], an optional [unit], and a food [item]. Given a line of text,
 * the class can determine whether it represents an ingredient; if so, it can parse out
 * the parts.
 *
 * On instantiation, the line is split into tokens; those tokens can be searched for
 * the components of an ingredient description.
 */
class IngredientParser(aLine: String) {
    private val tokens = aLine.split(' ', '\t',)
    private var amountTokens: Int = -1
    private var unitTokens: Int = -1
    private var itemTokens: Int = -1

    /**
     * The quantity of the item to use in a recipe.
     */
    var amount: Double = 0.0

    /**
     * The optional volume or weight of the item.
     */
    var unit: AUnit? = null

    /**
     * The type of food used.
     */
    lateinit var item: String

    private val nonIngredientKeywords = Preferences.getLanguageArray("nonIngredientKeywords")

    /**
     * This function grabs the amount of the food item from an ingredient string.
     */
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

    /**
     * This item finds the weight or volume [unit] of the food item in the ingredient string (or determines
     * that there is not one; for example, we usually don't weigh or measure eggs, we just count them).
     *
     * If the [amount] had not been previously found, this function will force that by calling [findAmount].
     */
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

    /**
     * This function gets the food [item], which is the remainder of the ingredient string after
     * the [amount] and [unit] have been isolated. If those had not been found previously, it
     * calls [findUnit] to force that.
     */
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

    /**
     * This function determines whether the string is actually an ingredient or not. Several
     * tests are used on the tokenized line:
     *
     * 1. Some localized keywords are recognized as recipe directions, usually given after
     *     all ingredients. If any of those are present, this is not an ingredient.
     * 1. The function [findIngredient] is called to separate the parts.
     * 1. Finally, the number of tokens in the food [item] part is counted. If greater than
     *     an arbitrary threshold, the line is assumed not to be an ingredient.
     *
     * @see findIngredient
     */
    fun isIngredient(): Boolean {
        if (tokens.containsAny(nonIngredientKeywords))
            return false
        if (itemTokens < 0) findIngredient()
        return itemTokens < 10
    }

    /**
     * Companion object that keeps class-level members.
     */
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
