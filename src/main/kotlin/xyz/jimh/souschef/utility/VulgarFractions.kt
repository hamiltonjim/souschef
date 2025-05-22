/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.utility

/**
 * Object holding constants for all Unicode vulgar fractions commonly used in recipes both as
 * [String] and [Char], along with constants that have the correct [Double] value for each.
 *
 * Naming:
 * - [ONE_HALF] is the Double value
 * - [ST_ONE_HALF] is the String
 * - [CH_ONE_HALF] is the character
 */
object VulgarFractions {
    const val ONE_HALF = 1.0 / 2.0
    const val ST_ONE_HALF = "½"
    const val CH_ONE_HALF = '½'

    const val ONE_THIRD = 1.0 / 3.0
    const val TWO_THIRDS = 2.0 / 3.0
    const val ST_ONE_THIRD = "⅓"
    const val ST_TWO_THIRDS = "⅔"
    const val CH_ONE_THIRD = '⅓'
    const val CH_TWO_THIRDS = '⅔'

    const val ONE_QUARTER = 1.0 / 4.0
    const val THREE_QUARTERS = 3.0 / 4.0
    const val ST_ONE_QUARTER = "¼"
    const val ST_THREE_QUARTERS = "¾"
    const val CH_ONE_QUARTER = '¼'
    const val CH_THREE_QUARTERS = '¾'

    const val ONE_FIFTH = 1.0 / 5.0
    const val TWO_FIFTHS = 2.0 / 5.0
    const val THREE_FIFTHS = 3.0 / 5.0
    const val FOUR_FIFTHS = 4.0 / 5.0
    const val ST_ONE_FIFTH = "⅕"
    const val ST_TWO_FIFTHS = "⅖"
    const val ST_THREE_FIFTHS = "⅗"
    const val ST_FOUR_FIFTHS = "⅘"
    const val CH_ONE_FIFTH = '⅕'
    const val CH_TWO_FIFTHS = '⅖'
    const val CH_THREE_FIFTHS = '⅗'
    const val CH_FOUR_FIFTHS = '⅘'

    const val ONE_SIXTH = 1.0 / 6.0
    const val FIVE_SIXTHS = 5.0 / 6.0
    const val ST_ONE_SIXTH = "⅙"
    const val ST_FIVE_SIXTHS = "⅚"
    const val CH_ONE_SIXTH = '⅙'
    const val CH_FIVE_SIXTHS = '⅚'

    const val ONE_EIGHTH = 1.0 / 8.0
    const val THREE_EIGHTHS = 3.0 / 8.0
    const val FIVE_EIGHTHS = 5.0 / 8.0
    const val SEVEN_EIGHTHS = 7.0 / 8.0
    const val ST_ONE_EIGHTH = "⅛"
    const val ST_THREE_EIGHTHS = "⅜"
    const val ST_FIVE_EIGHTHS = "⅝"
    const val ST_SEVEN_EIGHTHS = "⅞"
    const val CH_ONE_EIGHTH = '⅛'
    const val CH_THREE_EIGHTHS = '⅜'
    const val CH_FIVE_EIGHTHS = '⅝'
    const val CH_SEVEN_EIGHTHS = '⅞'

    internal const val ALL = "½⅓⅔¼¾⅕⅖⅗⅘⅙⅚⅛⅜⅝⅞"
}

/**
 * Function that returns true if the target is a "common" vulgar fraction, and false otherwise.
 */
fun Char.isVulgarFraction(): Boolean {
    return VulgarFractions.ALL.contains(this)
}