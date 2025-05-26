package xyz.jimh.souschef.parse

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import xyz.jimh.souschef.parse.NumberReader.FRACTION_NUMERATOR_ONE
import xyz.jimh.souschef.parse.NumberReader.FRACTION_SLASH
import xyz.jimh.souschef.parse.NumberReader.SUB_EIGHT
import xyz.jimh.souschef.parse.NumberReader.SUB_FIVE
import xyz.jimh.souschef.parse.NumberReader.SUB_FOUR
import xyz.jimh.souschef.parse.NumberReader.SUB_NINE
import xyz.jimh.souschef.parse.NumberReader.SUB_ONE
import xyz.jimh.souschef.parse.NumberReader.SUB_SEVEN
import xyz.jimh.souschef.parse.NumberReader.SUB_SIX
import xyz.jimh.souschef.parse.NumberReader.SUB_THREE
import xyz.jimh.souschef.parse.NumberReader.SUB_TWO
import xyz.jimh.souschef.parse.NumberReader.SUB_ZERO
import xyz.jimh.souschef.parse.NumberReader.SUPER_EIGHT
import xyz.jimh.souschef.parse.NumberReader.SUPER_FIVE
import xyz.jimh.souschef.parse.NumberReader.SUPER_FOUR
import xyz.jimh.souschef.parse.NumberReader.SUPER_NINE
import xyz.jimh.souschef.parse.NumberReader.SUPER_ONE
import xyz.jimh.souschef.parse.NumberReader.SUPER_SEVEN
import xyz.jimh.souschef.parse.NumberReader.SUPER_SIX
import xyz.jimh.souschef.parse.NumberReader.SUPER_THREE
import xyz.jimh.souschef.parse.NumberReader.SUPER_TWO
import xyz.jimh.souschef.parse.NumberReader.SUPER_ZERO
import xyz.jimh.souschef.utility.MathUtils.EPSILON
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

class NumberReaderTest {

    @Test
    fun `parseNumber test`() {
        assertAll(
            { assertEquals(11.0, NumberReader.parseNumber("11"), EPSILON) },
            { assertEquals(11.0, NumberReader.parseNumber("  11  "), EPSILON) },
            { assertEquals(11.5, NumberReader.parseNumber("11$ST_ONE_HALF"), EPSILON) },
            { assertEquals(11.5136, NumberReader.parseNumber("11.5136"), 1e-6) },
            { assertEquals(0.5, NumberReader.parseNumber(ST_ONE_HALF), EPSILON) },
            { assertEquals(1.0 / 3.0, NumberReader.parseNumber(ST_ONE_THIRD), EPSILON) },
            { assertEquals(2.0 / 3.0, NumberReader.parseNumber(ST_TWO_THIRDS), EPSILON) },
            { assertEquals(0.25, NumberReader.parseNumber(ST_ONE_QUARTER), EPSILON) },
            { assertEquals(0.75, NumberReader.parseNumber(ST_THREE_QUARTERS), EPSILON) },
            { assertEquals(0.2, NumberReader.parseNumber(ST_ONE_FIFTH), EPSILON) },
            { assertEquals(0.4, NumberReader.parseNumber(ST_TWO_FIFTHS), EPSILON) },
            { assertEquals(0.6, NumberReader.parseNumber(ST_THREE_FIFTHS), EPSILON) },
            { assertEquals(0.8, NumberReader.parseNumber(ST_FOUR_FIFTHS), EPSILON) },
            { assertEquals(1.0 / 6.0, NumberReader.parseNumber(ST_ONE_SIXTH), EPSILON) },
            { assertEquals(5.0 / 6.0, NumberReader.parseNumber(ST_FIVE_SIXTHS), EPSILON) },
            { assertEquals(0.125, NumberReader.parseNumber(ST_ONE_EIGHTH), EPSILON) },
            { assertEquals(0.375, NumberReader.parseNumber(ST_THREE_EIGHTHS), EPSILON) },
            { assertEquals(0.625, NumberReader.parseNumber(ST_FIVE_EIGHTHS), EPSILON) },
            { assertEquals(0.875, NumberReader.parseNumber(ST_SEVEN_EIGHTHS), EPSILON) },
            { assertEquals(0.888888889, NumberReader.parseNumber("8${FRACTION_SLASH}9"), EPSILON) },
            { assertEquals(0.888888889, NumberReader.parseNumber("8/9"), EPSILON) },
            { assertEquals(1.888888889, NumberReader.parseNumber("1 8/9"), EPSILON) },
            { assertEquals(2.0, NumberReader.parseNumber("18/9"), EPSILON) },
            { assertEquals(1.0 / 11.0, NumberReader.parseNumber("${FRACTION_NUMERATOR_ONE}11"), EPSILON) },
            { assertEquals(3 + 1.0 / 11.0, NumberReader.parseNumber("3${FRACTION_NUMERATOR_ONE}11"), EPSILON) },
            { assertEquals(3 + 1.0 / 11.0, NumberReader.parseNumber("3 ${FRACTION_NUMERATOR_ONE}11"), EPSILON) },
            { assertThrows<NumberFormatException> { NumberReader.parseNumber("${FRACTION_NUMERATOR_ONE}x11") }},
            { assertThrows<NumberFormatException> { NumberReader.parseNumber("x${FRACTION_NUMERATOR_ONE}11") }},
            { assertThrows<NumberFormatException> { NumberReader.parseNumber("3x ${FRACTION_NUMERATOR_ONE}11") }},
            { assertThrows<NumberFormatException> { NumberReader.parseNumber("3${FRACTION_NUMERATOR_ONE}11/11d") }},
            { assertEquals(3045.0 / 111.0, NumberReader.parseNumber("30$SUPER_FOUR$SUB_FIVE/${SUB_ONE}1$SUPER_ONE"), EPSILON) },
            { assertEquals(345.0, NumberReader.parseNumber("3$SUPER_FOUR$SUB_FIVE"), EPSILON) },
            { assertEquals(317.0 / 11.0, NumberReader.parseNumber("\t3$SUPER_ONE$SUB_SEVEN/${SUB_ONE}1"), EPSILON) },
            { assertEquals(817.0 / 911.0, NumberReader.parseNumber("8$SUPER_ONE$SUB_SEVEN/9${SUB_ONE}1"), EPSILON) },
            { assertEquals(123.0 / 456.0, NumberReader.parseNumber("${SUPER_ZERO}123/${SUB_ZERO}456"), EPSILON) },
            { assertEquals(123.0 / 456.0, NumberReader.parseNumber("$SUPER_ONE$SUPER_TWO$SUPER_THREE/$SUPER_FOUR$SUPER_FIVE$SUPER_SIX"), EPSILON) },
            { assertEquals(123.0 / 456.0, NumberReader.parseNumber("$SUB_ONE$SUB_TWO$SUB_THREE/$SUB_FOUR$SUB_FIVE$SUB_SIX"), EPSILON) },
            { assertEquals(78.0 / 90.0, NumberReader.parseNumber("$SUPER_SEVEN$SUPER_EIGHT/$SUPER_NINE$SUPER_ZERO"), EPSILON) },
            { assertEquals(78.0 / 90.0, NumberReader.parseNumber("$SUB_SEVEN$SUB_EIGHT/$SUB_NINE$SUB_ZERO"), EPSILON) },
            { assertEquals(78.0 / 90.0, NumberReader.parseNumber("${SUB_ZERO}78/${SUPER_ZERO}90"), EPSILON) },
            { assertEquals(78.9, NumberReader.parseNumber("78.$SUB_NINE"), EPSILON) },
            { assertEquals(0.123456789, NumberReader.parseNumber("0.123456789"), 1e-10) },
            { assertEquals(0.123456789, NumberReader.parseNumber("0.12345678$SUB_NINE"), 1e-10) },
            { assertEquals(0.123456789, NumberReader.parseNumber("0.12345678$SUPER_NINE"), 1e-10) },
            { assertEquals(0.123456789, NumberReader.parseNumber("0.1234567${SUB_EIGHT}9"), 1e-10) },
            { assertEquals(0.123456789, NumberReader.parseNumber("0.1234567${SUPER_EIGHT}9"), 1e-10) },
            { assertEquals(0.123456789, NumberReader.parseNumber("0.123456${SUB_SEVEN}89"), 1e-10) },
            { assertEquals(0.123456789, NumberReader.parseNumber("0.123456${SUPER_SEVEN}89"), 1e-10) },
            { assertEquals(0.123456789, NumberReader.parseNumber("0.12345${SUB_SIX}789"), 1e-10) },
            { assertEquals(0.123456789, NumberReader.parseNumber("0.12345${SUPER_SIX}789"), 1e-10) },
            { assertEquals(0.123456789, NumberReader.parseNumber("0.1234${SUB_FIVE}6789"), 1e-10) },
            { assertEquals(0.123456789, NumberReader.parseNumber("0.1234${SUPER_FIVE}6789"), 1e-10) },
            { assertEquals(0.123456789, NumberReader.parseNumber("0.123${SUB_FOUR}56789"), 1e-10) },
            { assertEquals(0.123456789, NumberReader.parseNumber("0.123${SUPER_FOUR}56789"), 1e-10) },
            { assertEquals(0.123456789, NumberReader.parseNumber("0.12${SUB_THREE}456789"), 1e-10) },
            { assertEquals(0.123456789, NumberReader.parseNumber("0.12${SUPER_THREE}456789"), 1e-10) },
            { assertEquals(0.123456789, NumberReader.parseNumber("0.1${SUB_TWO}3456789"), 1e-10) },
            { assertEquals(0.123456789, NumberReader.parseNumber("0.1${SUPER_TWO}3456789"), 1e-10) },
            { assertEquals(0.123456789, NumberReader.parseNumber("0.${SUB_ONE}23456789"), 1e-10) },
            { assertEquals(0.123456789, NumberReader.parseNumber("0.${SUPER_ONE}23456789"), 1e-10) },
            { assertEquals(0.023456789, NumberReader.parseNumber("0.${SUB_ZERO}23456789"), 1e-10) },
            { assertEquals(0.023456789, NumberReader.parseNumber("0.${SUPER_ZERO}234567890"), 1e-10) },
            { assertTrue(NumberReader.isNumber("$SUB_ZERO.$SUPER_ZERO")) },
            { assertThrows<NumberFormatException> { NumberReader.parseNumber("0.${SUPER_ZERO}2345678z9") } },
        )
    }

}