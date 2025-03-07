/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.display

import org.springframework.stereotype.Component
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.config.UnitAbbrev
import xyz.jimh.souschef.config.round
import xyz.jimh.souschef.data.AUnit
import xyz.jimh.souschef.data.UnitDao
import xyz.jimh.souschef.utility.MathUtils

@Component
class IngredientFormatter(private val unitDao: UnitDao) {

    companion object {
        private const val ONE_HALF = 1.0 / 2.0
        private const val CH_ONE_HALF = "½"

        private const val ONE_THIRD = 1.0 / 3.0
        private const val TWO_THIRDS = 2.0 / 3.0
        private const val CH_ONE_THIRD = "⅓"
        private const val CH_TWO_THIRDS = "⅔"

        private const val ONE_QUARTER = 1.0 / 4.0
        private const val THREE_QUARTERS = 3.0 / 4.0
        private const val CH_ONE_QUARTER = "¼"
        private const val CH_THREE_QUARTERS = "¾"

        private const val ONE_EIGHTH = 1.0 / 8.0
        private const val THREE_EIGHTHS = 3.0 / 8.0
        private const val FIVE_EIGHTHS = 5.0 / 8.0
        private const val SEVEN_EIGHTHS = 7.0 / 8.0
        private const val CH_ONE_EIGHTH = "⅛"
        private const val CH_THREE_EIGHTHS = "⅜"
        private const val CH_FIVE_EIGHTHS = "⅝"
        private const val CH_SEVEN_EIGHTHS = "⅞"
    }

    fun writeUnit(remoteHost: String, unitName: String): String {
        val unit: AUnit = unitDao.findByName(unitName) ?: unitDao.findByAbbrev(unitName) ?: return ""

        return when (Preferences.getUnitNames(remoteHost)) {
            UnitAbbrev.FULL_NAME -> unit.name
            UnitAbbrev.ABBREVIATION -> {
                var abbrev = unit.abbrev
                if (abbrev.isNullOrBlank()) {
                    abbrev = unit.name
                }
                abbrev
            }
        }
    }

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
}