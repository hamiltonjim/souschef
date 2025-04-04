/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.display

import org.springframework.stereotype.Component
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.config.UnitAbbrev
import xyz.jimh.souschef.data.AUnit
import xyz.jimh.souschef.data.Ingredient
import xyz.jimh.souschef.data.Preference
import xyz.jimh.souschef.data.Recipe
import xyz.jimh.souschef.data.UnitDao
import xyz.jimh.souschef.utility.MathUtils
import xyz.jimh.souschef.utility.round

/**
 * Bean that formats the various parts of an [Ingredient] in a
 * displayed [Recipe].
 * @constructor Automagically built with [UnitDao].
 */
@Component
class IngredientFormatter(private val unitDao: UnitDao) {

    /**
     * Get the correct string for the [unitName] (full or abbrev.),
     * as per the [Preference] for the [remoteHost].
     */
    fun writeUnit(remoteHost: String, unitName: String): String {
        val unit: AUnit = unitDao.findByName(unitName) ?: unitDao.findByAbbrev(unitName) ?: return ""

        return when (Preferences.getUnitNames(remoteHost)) {
            UnitAbbrev.FULL_NAME -> unit.name
            UnitAbbrev.ABBREVIATION -> {
                if (unit.abbrev.isNullOrBlank())
                    unit.name
                else
                    unit.abbrev!!
            }
        }
    }

    /**
     * Write the given [number], possibly with a fraction (such as 1½)
     */
    fun writeNumber(number: Double): String {
        val intPart: Int = (number + MathUtils.EPSILON).toInt()
        val fraction = number.minus(intPart)

        return if (MathUtils.eqEpsilon(fraction, 0.0)) {
            "$intPart"
        } else if (intPart == 0) {
            fractional(fraction)
        } else {
            "$intPart${fractional(fraction)}"
        }
    }

    /**
     * Write the given [number] as a [String], except that zero
     * should be written as an empty string.
     */
    fun writePlainNumber(number: Double): String {
        return when (number) {
            0.0 -> ""
            else -> "$number"
        }
    }

    private fun fractional(fraction: Double): String {
        if (MathUtils.eqEpsilon(fraction, ONE_HALF)) {
            return CH_ONE_HALF
        } else if (MathUtils.eqEpsilon(fraction, ONE_THIRD)) {
            return CH_ONE_THIRD
        } else if (MathUtils.eqEpsilon(fraction, TWO_THIRDS)) {
            return CH_TWO_THIRDS
        } else if (MathUtils.eqEpsilon(fraction, ONE_QUARTER)) {
            return CH_ONE_QUARTER
        } else if (MathUtils.eqEpsilon(fraction, THREE_QUARTERS)) {
            return CH_THREE_QUARTERS
        } else if (MathUtils.eqEpsilon(fraction, ONE_EIGHTH)) {
            return CH_ONE_EIGHTH
        } else if (MathUtils.eqEpsilon(fraction, THREE_EIGHTHS)) {
            return CH_THREE_EIGHTHS
        } else if (MathUtils.eqEpsilon(fraction, FIVE_EIGHTHS)) {
            return CH_FIVE_EIGHTHS
        } else if (MathUtils.eqEpsilon(fraction, SEVEN_EIGHTHS)) {
            return CH_SEVEN_EIGHTHS
        } else {
            return "${fraction.round(2)}".substring(1)
        }
    }

    /**
     * Certain fractions and the right (single) character for each.
     */
    companion object {
        internal const val ONE_HALF = 1.0 / 2.0
        internal const val CH_ONE_HALF = "½"

        internal const val ONE_THIRD = 1.0 / 3.0
        internal const val TWO_THIRDS = 2.0 / 3.0
        internal const val CH_ONE_THIRD = "⅓"
        internal const val CH_TWO_THIRDS = "⅔"

        internal const val ONE_QUARTER = 1.0 / 4.0
        internal const val THREE_QUARTERS = 3.0 / 4.0
        internal const val CH_ONE_QUARTER = "¼"
        internal const val CH_THREE_QUARTERS = "¾"

        internal const val ONE_EIGHTH = 1.0 / 8.0
        internal const val THREE_EIGHTHS = 3.0 / 8.0
        internal const val FIVE_EIGHTHS = 5.0 / 8.0
        internal const val SEVEN_EIGHTHS = 7.0 / 8.0
        internal const val CH_ONE_EIGHTH = "⅛"
        internal const val CH_THREE_EIGHTHS = "⅜"
        internal const val CH_FIVE_EIGHTHS = "⅝"
        internal const val CH_SEVEN_EIGHTHS = "⅞"
    }
}