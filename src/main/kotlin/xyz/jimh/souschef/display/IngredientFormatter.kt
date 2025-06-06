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
import xyz.jimh.souschef.parse.NumberReader.ST_EIGHT_NINTHS
import xyz.jimh.souschef.parse.NumberReader.ST_FIVE_NINTHS
import xyz.jimh.souschef.parse.NumberReader.ST_FIVE_SEVENTHS
import xyz.jimh.souschef.parse.NumberReader.ST_FOUR_NINTHS
import xyz.jimh.souschef.parse.NumberReader.ST_FOUR_SEVENTHS
import xyz.jimh.souschef.parse.NumberReader.ST_NINE_TENTHS
import xyz.jimh.souschef.parse.NumberReader.ST_ONE_NINTH
import xyz.jimh.souschef.parse.NumberReader.ST_ONE_SEVENTH
import xyz.jimh.souschef.parse.NumberReader.ST_ONE_TENTH
import xyz.jimh.souschef.parse.NumberReader.ST_SEVEN_NINTHS
import xyz.jimh.souschef.parse.NumberReader.ST_SEVEN_TENTHS
import xyz.jimh.souschef.parse.NumberReader.ST_SIX_SEVENTHS
import xyz.jimh.souschef.parse.NumberReader.ST_THREE_SEVENTHS
import xyz.jimh.souschef.parse.NumberReader.ST_THREE_TENTHS
import xyz.jimh.souschef.parse.NumberReader.ST_TWO_NINTHS
import xyz.jimh.souschef.parse.NumberReader.ST_TWO_SEVENTHS
import xyz.jimh.souschef.utility.MathUtils
import xyz.jimh.souschef.utility.VulgarFractions.EIGHT_NINTHS
import xyz.jimh.souschef.utility.VulgarFractions.FIVE_EIGHTHS
import xyz.jimh.souschef.utility.VulgarFractions.FIVE_NINTHS
import xyz.jimh.souschef.utility.VulgarFractions.FIVE_SEVENTHS
import xyz.jimh.souschef.utility.VulgarFractions.FIVE_SIXTHS
import xyz.jimh.souschef.utility.VulgarFractions.FOUR_FIFTHS
import xyz.jimh.souschef.utility.VulgarFractions.FOUR_NINTHS
import xyz.jimh.souschef.utility.VulgarFractions.FOUR_SEVENTHS
import xyz.jimh.souschef.utility.VulgarFractions.NINE_TENTHS
import xyz.jimh.souschef.utility.VulgarFractions.ONE_EIGHTH
import xyz.jimh.souschef.utility.VulgarFractions.ONE_FIFTH
import xyz.jimh.souschef.utility.VulgarFractions.ONE_HALF
import xyz.jimh.souschef.utility.VulgarFractions.ONE_NINTH
import xyz.jimh.souschef.utility.VulgarFractions.ONE_QUARTER
import xyz.jimh.souschef.utility.VulgarFractions.ONE_SEVENTH
import xyz.jimh.souschef.utility.VulgarFractions.ONE_SIXTH
import xyz.jimh.souschef.utility.VulgarFractions.ONE_TENTH
import xyz.jimh.souschef.utility.VulgarFractions.ONE_THIRD
import xyz.jimh.souschef.utility.VulgarFractions.SEVEN_EIGHTHS
import xyz.jimh.souschef.utility.VulgarFractions.SEVEN_NINTHS
import xyz.jimh.souschef.utility.VulgarFractions.SEVEN_TENTHS
import xyz.jimh.souschef.utility.VulgarFractions.SIX_SEVENTHS
import xyz.jimh.souschef.utility.VulgarFractions.ST_FIVE_EIGHTHS
import xyz.jimh.souschef.utility.VulgarFractions.ST_FIVE_SIXTHS
import xyz.jimh.souschef.utility.VulgarFractions.ST_FOUR_FIFTHS
import xyz.jimh.souschef.utility.VulgarFractions.ST_ONE_EIGHTH
import xyz.jimh.souschef.utility.VulgarFractions.ST_ONE_FIFTH
import xyz.jimh.souschef.utility.VulgarFractions.ST_ONE_HALF
import xyz.jimh.souschef.utility.VulgarFractions.ST_ONE_QUARTER
import xyz.jimh.souschef.utility.VulgarFractions.ST_ONE_SIXTH
import xyz.jimh.souschef.utility.VulgarFractions.ST_ONE_THIRD
import xyz.jimh.souschef.utility.VulgarFractions.ST_SEVEN_EIGHTHS
import xyz.jimh.souschef.utility.VulgarFractions.ST_THREE_EIGHTHS
import xyz.jimh.souschef.utility.VulgarFractions.ST_THREE_FIFTHS
import xyz.jimh.souschef.utility.VulgarFractions.ST_THREE_QUARTERS
import xyz.jimh.souschef.utility.VulgarFractions.ST_TWO_FIFTHS
import xyz.jimh.souschef.utility.VulgarFractions.ST_TWO_THIRDS
import xyz.jimh.souschef.utility.VulgarFractions.THREE_EIGHTHS
import xyz.jimh.souschef.utility.VulgarFractions.THREE_FIFTHS
import xyz.jimh.souschef.utility.VulgarFractions.THREE_QUARTERS
import xyz.jimh.souschef.utility.VulgarFractions.THREE_SEVENTHS
import xyz.jimh.souschef.utility.VulgarFractions.THREE_TENTHS
import xyz.jimh.souschef.utility.VulgarFractions.TWO_FIFTHS
import xyz.jimh.souschef.utility.VulgarFractions.TWO_NINTHS
import xyz.jimh.souschef.utility.VulgarFractions.TWO_SEVENTHS
import xyz.jimh.souschef.utility.VulgarFractions.TWO_THIRDS
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
        val unit: AUnit = unitDao.findByName(unitName) ?:
            unitDao.findByAbbrev(unitName) ?:
            unitDao.findByAltAbbrev(unitName) ?:
            return ""

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
     * Write the given [number], possibly with a fraction (so 1.5 would be rendered as  '1½')
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
        return when {
            MathUtils.eqEpsilon(fraction, ONE_HALF) -> ST_ONE_HALF

            MathUtils.eqEpsilon(fraction, ONE_THIRD) -> ST_ONE_THIRD
            MathUtils.eqEpsilon(fraction, TWO_THIRDS) -> ST_TWO_THIRDS

            MathUtils.eqEpsilon(fraction, ONE_QUARTER) -> ST_ONE_QUARTER
            MathUtils.eqEpsilon(fraction, THREE_QUARTERS) -> ST_THREE_QUARTERS

            MathUtils.eqEpsilon(fraction, ONE_FIFTH) -> ST_ONE_FIFTH
            MathUtils.eqEpsilon(fraction, TWO_FIFTHS) -> ST_TWO_FIFTHS
            MathUtils.eqEpsilon(fraction, THREE_FIFTHS) -> ST_THREE_FIFTHS
            MathUtils.eqEpsilon(fraction, FOUR_FIFTHS) -> ST_FOUR_FIFTHS

            MathUtils.eqEpsilon(fraction, ONE_SIXTH) -> ST_ONE_SIXTH
            MathUtils.eqEpsilon(fraction, FIVE_SIXTHS) -> ST_FIVE_SIXTHS

            MathUtils.eqEpsilon(fraction, ONE_SEVENTH) -> ST_ONE_SEVENTH
            MathUtils.eqEpsilon(fraction, TWO_SEVENTHS) -> ST_TWO_SEVENTHS
            MathUtils.eqEpsilon(fraction, THREE_SEVENTHS) -> ST_THREE_SEVENTHS
            MathUtils.eqEpsilon(fraction, FOUR_SEVENTHS) -> ST_FOUR_SEVENTHS
            MathUtils.eqEpsilon(fraction, FIVE_SEVENTHS) -> ST_FIVE_SEVENTHS
            MathUtils.eqEpsilon(fraction, SIX_SEVENTHS) -> ST_SIX_SEVENTHS

            MathUtils.eqEpsilon(fraction, ONE_EIGHTH) -> ST_ONE_EIGHTH
            MathUtils.eqEpsilon(fraction, THREE_EIGHTHS) -> ST_THREE_EIGHTHS
            MathUtils.eqEpsilon(fraction, FIVE_EIGHTHS) -> ST_FIVE_EIGHTHS
            MathUtils.eqEpsilon(fraction, SEVEN_EIGHTHS) -> ST_SEVEN_EIGHTHS

            MathUtils.eqEpsilon(fraction, ONE_NINTH) -> ST_ONE_NINTH
            MathUtils.eqEpsilon(fraction, TWO_NINTHS) -> ST_TWO_NINTHS
            MathUtils.eqEpsilon(fraction, FOUR_NINTHS) -> ST_FOUR_NINTHS
            MathUtils.eqEpsilon(fraction, FIVE_NINTHS) -> ST_FIVE_NINTHS
            MathUtils.eqEpsilon(fraction, SEVEN_NINTHS) -> ST_SEVEN_NINTHS
            MathUtils.eqEpsilon(fraction, EIGHT_NINTHS) -> ST_EIGHT_NINTHS

            MathUtils.eqEpsilon(fraction, ONE_TENTH) -> ST_ONE_TENTH
            MathUtils.eqEpsilon(fraction, THREE_TENTHS) -> ST_THREE_TENTHS
            MathUtils.eqEpsilon(fraction, SEVEN_TENTHS) -> ST_SEVEN_TENTHS
            MathUtils.eqEpsilon(fraction, NINE_TENTHS) -> ST_NINE_TENTHS

            else -> "${fraction.round(2)}".substring(1)
        }
    }
}