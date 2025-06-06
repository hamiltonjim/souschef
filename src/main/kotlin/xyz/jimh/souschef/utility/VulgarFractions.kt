/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.utility

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

/**
 * Object holding constants for all Unicode vulgar fractions commonly used in recipes both as
 * [String] and [Char], along with constants that have the correct [Double] value for each.
 *
 * Naming:
 * - [ONE_HALF] is the Double value
 * - [ST_ONE_HALF] is the String
 * - [CH_ONE_HALF] is the character
 *
 * All properties are access level internal; please see the source code for definitions.
 * "Use the source, Luke!"
 */
object VulgarFractions {
    internal const val ONE_HALF = 1.0 / 2.0
    internal const val ST_ONE_HALF = "½"
    internal const val CH_ONE_HALF = '½'

    internal const val ONE_THIRD = 1.0 / 3.0
    internal const val TWO_THIRDS = 2.0 / 3.0
    internal const val ST_ONE_THIRD = "⅓"
    internal const val ST_TWO_THIRDS = "⅔"
    internal const val CH_ONE_THIRD = '⅓'
    internal const val CH_TWO_THIRDS = '⅔'

    internal const val ONE_QUARTER = 1.0 / 4.0
    internal const val THREE_QUARTERS = 3.0 / 4.0
    internal const val ST_ONE_QUARTER = "¼"
    internal const val ST_THREE_QUARTERS = "¾"
    internal const val CH_ONE_QUARTER = '¼'
    internal const val CH_THREE_QUARTERS = '¾'

    internal const val ONE_FIFTH = 1.0 / 5.0
    internal const val TWO_FIFTHS = 2.0 / 5.0
    internal const val THREE_FIFTHS = 3.0 / 5.0
    internal const val FOUR_FIFTHS = 4.0 / 5.0
    internal const val ST_ONE_FIFTH = "⅕"
    internal const val ST_TWO_FIFTHS = "⅖"
    internal const val ST_THREE_FIFTHS = "⅗"
    internal const val ST_FOUR_FIFTHS = "⅘"
    internal const val CH_ONE_FIFTH = '⅕'
    internal const val CH_TWO_FIFTHS = '⅖'
    internal const val CH_THREE_FIFTHS = '⅗'
    internal const val CH_FOUR_FIFTHS = '⅘'

    internal const val ONE_SIXTH = 1.0 / 6.0
    internal const val FIVE_SIXTHS = 5.0 / 6.0
    internal const val ST_ONE_SIXTH = "⅙"
    internal const val ST_FIVE_SIXTHS = "⅚"
    internal const val CH_ONE_SIXTH = '⅙'
    internal const val CH_FIVE_SIXTHS = '⅚'

    internal const val ONE_EIGHTH = 1.0 / 8.0
    internal const val THREE_EIGHTHS = 3.0 / 8.0
    internal const val FIVE_EIGHTHS = 5.0 / 8.0
    internal const val SEVEN_EIGHTHS = 7.0 / 8.0
    internal const val ST_ONE_EIGHTH = "⅛"
    internal const val ST_THREE_EIGHTHS = "⅜"
    internal const val ST_FIVE_EIGHTHS = "⅝"
    internal const val ST_SEVEN_EIGHTHS = "⅞"
    internal const val CH_ONE_EIGHTH = '⅛'
    internal const val CH_THREE_EIGHTHS = '⅜'
    internal const val CH_FIVE_EIGHTHS = '⅝'
    internal const val CH_SEVEN_EIGHTHS = '⅞'

    internal const val ONE_SEVENTH = 1.0 / 7.0
    internal const val TWO_SEVENTHS = 2.0 / 7.0
    internal const val THREE_SEVENTHS = 3.0 / 7.0
    internal const val FOUR_SEVENTHS = 4.0 / 7.0
    internal const val FIVE_SEVENTHS = 5.0 / 7.0
    internal const val SIX_SEVENTHS = 6.0 / 7.0

    internal const val ONE_NINTH = 1.0 / 9.0
    internal const val TWO_NINTHS = 2.0 / 9.0
    internal const val FOUR_NINTHS = 4.0 / 9.0
    internal const val FIVE_NINTHS = 5.0 / 9.0
    internal const val SEVEN_NINTHS = 7.0 / 9.0
    internal const val EIGHT_NINTHS = 8.0 / 9.0

    internal const val ONE_TENTH = 0.1
    internal const val THREE_TENTHS = 0.3
    internal const val SEVEN_TENTHS = 0.7
    internal const val NINE_TENTHS = 0.9

    internal const val ALL = "½⅓⅔¼¾⅕⅖⅗⅘⅙⅚⅛⅜⅝⅞" +
            "$ST_ONE_SEVENTH$ST_TWO_SEVENTHS$ST_THREE_SEVENTHS$ST_FOUR_SEVENTHS$ST_FIVE_SEVENTHS$ST_SIX_SEVENTHS" +
            "$ST_ONE_NINTH$ST_TWO_NINTHS$ST_FOUR_NINTHS$ST_FIVE_NINTHS$ST_SEVEN_NINTHS$ST_EIGHT_NINTHS" +
            "$ST_ONE_TENTH$ST_THREE_TENTHS$ST_SEVEN_TENTHS$ST_NINE_TENTHS"
}

/**
 * Function that returns true if the target is a "common" vulgar fraction, and false otherwise.
 */
fun Char.isVulgarFraction(): Boolean {
    return VulgarFractions.ALL.contains(this)
}