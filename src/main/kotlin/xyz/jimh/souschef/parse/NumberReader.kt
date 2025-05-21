/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.parse

import xyz.jimh.souschef.parse.NumberReader.FRACTION_NUMERATOR_ONE
import xyz.jimh.souschef.parse.NumberReader.FRACTION_SLASH
import xyz.jimh.souschef.utility.VulgarFractions.CH_FIVE_EIGHTHS
import xyz.jimh.souschef.utility.VulgarFractions.CH_FIVE_SIXTHS
import xyz.jimh.souschef.utility.VulgarFractions.CH_FOUR_FIFTHS
import xyz.jimh.souschef.utility.VulgarFractions.CH_ONE_EIGHTH
import xyz.jimh.souschef.utility.VulgarFractions.CH_ONE_FIFTH
import xyz.jimh.souschef.utility.VulgarFractions.CH_ONE_HALF
import xyz.jimh.souschef.utility.VulgarFractions.CH_ONE_QUARTER
import xyz.jimh.souschef.utility.VulgarFractions.CH_ONE_SIXTH
import xyz.jimh.souschef.utility.VulgarFractions.CH_ONE_THIRD
import xyz.jimh.souschef.utility.VulgarFractions.CH_SEVEN_EIGHTHS
import xyz.jimh.souschef.utility.VulgarFractions.CH_THREE_EIGHTHS
import xyz.jimh.souschef.utility.VulgarFractions.CH_THREE_FIFTHS
import xyz.jimh.souschef.utility.VulgarFractions.CH_THREE_QUARTERS
import xyz.jimh.souschef.utility.VulgarFractions.CH_TWO_FIFTHS
import xyz.jimh.souschef.utility.VulgarFractions.CH_TWO_THIRDS
import xyz.jimh.souschef.utility.isVulgarFraction

/**
 * Object that reads a number, consisting of (potentially) a whole part and a
 * fraction part, either of which may be missing. This object will recognize all
 * of the following as "three and a half:"
 * - 3.5
 * - 3 1/2
 * - 3½
 * - 3⅟2
 * - 3 ⅟2
 *
 * In the second form, both the ASCII / (0x27) and the Unicode [FRACTION_SLASH]
 * will be recognized as separating numerator and denominator.
 * In the fourth form, the Unicode [FRACTION_NUMERATOR_ONE] starts the fractional
 * part, without a space; the fifth form adds a space.
 */
object NumberReader {
    const val FRACTION_SLASH = '\u2044'
    const val FRACTION_NUMERATOR_ONE = '\u215f'

    const val SUPER_ZERO = '\u2070'
    const val SUPER_ONE = '\u00b9'
    const val SUPER_TWO = '\u00b2'
    const val SUPER_THREE = '\u00b3'
    const val SUPER_FOUR = '\u2074'
    const val SUPER_FIVE = '\u2075'
    const val SUPER_SIX = '\u2076'
    const val SUPER_SEVEN = '\u2077'
    const val SUPER_EIGHT = '\u2078'
    const val SUPER_NINE = '\u2079'

    const val SUB_ZERO = '\u2080'
    const val SUB_ONE = '\u2081'
    const val SUB_TWO = '\u2082'
    const val SUB_THREE = '\u2083'
    const val SUB_FOUR = '\u2084'
    const val SUB_FIVE = '\u2085'
    const val SUB_SIX = '\u2086'
    const val SUB_SEVEN = '\u2087'
    const val SUB_EIGHT = '\u2088'
    const val SUB_NINE = '\u2089'

    const val SUPERS = "$SUPER_ZERO$SUPER_ONE$SUPER_TWO$SUPER_THREE$SUPER_FOUR$SUPER_FIVE$SUPER_SIX$SUPER_SEVEN$SUPER_EIGHT$SUPER_NINE"
    const val SUBS = "$SUB_ZERO$SUB_ONE$SUB_TWO$SUB_THREE$SUB_FOUR$SUB_FIVE$SUB_SIX$SUB_SEVEN$SUB_EIGHT$SUB_NINE"
    const val SUPER_SUBS = "$SUPERS$SUBS"
    const val NUMBER_PUNCTUATION = "./,$FRACTION_SLASH$FRACTION_NUMERATOR_ONE"

    /**
     * Split the supposed numeric string into integer and fraction parts, and then separately evaluate
     * each part.
     *
     * @throws NumberFormatException if the value is not a number.
     */
    fun parseNumber(input: String): Double {
        val parts = splitNumber(input)
        return readInteger(parts.whole).toDouble() + readFraction(parts.fraction)
    }

    private fun splitNumber(input: String): ParsedNumber {
        val trimmed = input.trim()
        var index = 0
        while (index < trimmed.length) {
            val ch = trimmed[index]
            // are we actually in a fraction already?
            if (ch == '/' || ch == FRACTION_SLASH) {
                index = 0
                break
            }
            if (!Character.isDigit(ch) && !ch.isSuperOrSubDigit()) {
                break
            }
            ++index
        }
        if (index == 0) return ParsedNumber("", trimmed)

        val whole = trimmed.substring(0, index)
        val fraction = trimmed.substring(index)
        return ParsedNumber(whole, fraction)
    }

    /**
     * Converts the input to an integer, if possible.
     * @throws NumberFormatException if conversion fails
     */
    private fun readInteger(input: String): Int {
        if (input.isBlank()) {
            return 0
        }
        return try {
            input.toInt()
        } catch (e: NumberFormatException) {
            var value = 0
            for (c in input.toCharArray()) {
                val digit = when (c) {
                    '0', SUPER_ZERO, SUB_ZERO -> 0
                    '1', SUPER_ONE, SUB_ONE -> 1
                    '2', SUPER_TWO, SUB_TWO -> 2
                    '3', SUPER_THREE, SUB_THREE -> 3
                    '4', SUPER_FOUR, SUB_FOUR -> 4
                    '5', SUPER_FIVE, SUB_FIVE -> 5
                    '6', SUPER_SIX, SUB_SIX -> 6
                    '7', SUPER_SEVEN, SUB_SEVEN -> 7
                    '8', SUPER_EIGHT, SUB_EIGHT -> 8
                    '9', SUPER_NINE, SUB_NINE -> 9
                    else -> throw NumberFormatException("Invalid input: $input")
                }
                value *= 10
                value += digit
            }
            value
        }
    }

    private fun Char.isSuperOrSubDigit(): Boolean = SUPER_SUBS.contains(this)
    private fun Char.isNumberPunctuation(): Boolean = NUMBER_PUNCTUATION.contains(this)

    /**
     * Converts the input to a decimal fraction, if possible. Will
     * accept either a traditional "typewriter" fraction, such as
     * "1/2" or "5/6", or the Unicode fraction glyphs such as ½, ⅔,
     * and others.
     * @throws NumberFormatException if conversion fails
     */
    private fun readFraction(input: String): Double {
        if (input.isBlank()) {
            return 0.0
        }

        val string = input.trim()
        val slashFraction = string.split('/', FRACTION_SLASH, limit = 2)
        if (slashFraction.size == 2) {
            val numerator = readInteger(slashFraction[0])
            val denominator = readInteger(slashFraction[1])
            return numerator.toDouble() / denominator.toDouble()
        }
        return when (string[0]) {
            CH_ONE_HALF -> 0.5
            CH_ONE_THIRD -> 1.0 / 3.0
            CH_TWO_THIRDS -> 2.0 / 3.0
            CH_ONE_QUARTER -> 0.25
            CH_THREE_QUARTERS -> 0.75
            CH_ONE_FIFTH -> 0.2
            CH_TWO_FIFTHS -> 0.4
            CH_THREE_FIFTHS -> 0.6
            CH_FOUR_FIFTHS -> 0.8
            CH_ONE_SIXTH -> 1.0 / 6.0
            CH_FIVE_SIXTHS -> 5.0 / 6.0
            CH_ONE_EIGHTH -> 0.125
            CH_THREE_EIGHTHS -> 0.375
            CH_FIVE_EIGHTHS -> 0.625
            CH_SEVEN_EIGHTHS -> 0.875
            '.' -> string.parseToFraction()
            FRACTION_NUMERATOR_ONE -> 1.0 / string.substring(1).toDouble()
            else -> throw NumberFormatException("Invalid fraction: $string")
        }
    }

    /**
     * Checks whether [input] consists only of digits and the
     * punctuation used in numbers (slashes, dots, commas).
     */
    fun isNumber(input: String): Boolean {
        for (ch in input.toCharArray()) {
            if (!ch.isDigit() && !ch.isNumberPunctuation() && !ch.isSuperOrSubDigit() && !ch.isVulgarFraction()) {
                return false
            }
        }
        return true
    }

    fun String.parseToFraction(): Double {
        val stringBuilder = StringBuilder(".")
        this.forEachIndexed { index, ch ->
            if (index == 0) return@forEachIndexed
            val newCh = when (ch) {
                '0', SUPER_ZERO, SUB_ZERO -> '0'
                '1', SUPER_ONE, SUB_ONE -> '1'
                '2', SUPER_TWO, SUB_TWO -> '2'
                '3', SUPER_THREE, SUB_THREE -> '3'
                '4', SUPER_FOUR, SUB_FOUR -> '4'
                '5', SUPER_FIVE, SUB_FIVE -> '5'
                '6', SUPER_SIX, SUB_SIX -> '6'
                '7', SUPER_SEVEN, SUB_SEVEN -> '7'
                '8', SUPER_EIGHT, SUB_EIGHT -> '8'
                '9', SUPER_NINE, SUB_NINE -> '9'
                else -> ch
            }
            stringBuilder.append(newCh)
        }
        return stringBuilder.toString().toDouble()
    }
}