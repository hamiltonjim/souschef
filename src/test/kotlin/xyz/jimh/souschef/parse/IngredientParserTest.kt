package xyz.jimh.souschef.parse

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import xyz.jimh.souschef.ControllerTestBase
import xyz.jimh.souschef.config.UnitType
import xyz.jimh.souschef.data.AUnit
import xyz.jimh.souschef.data.UnitDao

class IngredientParserTest : ControllerTestBase() {
    val in1 = "3.5 cups flour"
    val in2 = "3 1/2 c. sugar"
    val in3 = "1 pound butter"
    val in4 = "6 eggs"
    val in5 = "1¼ pound powdered sugar"
    val in6 = "4 fluid ounces milk"
    val in7 = "200 ml. milk"
    val in8 = "1 T baking soda"

    val notIn = "now is the time for all good men to come to the aid of their party"

    val parser1 = IngredientParser(in1)
    val parser2 = IngredientParser(in2)
    val parser3 = IngredientParser(in3)
    val parser4 = IngredientParser(in4)
    val parser5 = IngredientParser(in5)
    val parser6 = IngredientParser(in6)
    val parser7 = IngredientParser(in7)
    val parser8 = IngredientParser(in8)

    val emptyParser = IngredientParser("")
    val notIngredParser = IngredientParser(notIn)

    private lateinit var unitDao: UnitDao

    @BeforeEach
    fun setup() {
        setupContext()

        unitDao = mockk()
        IngredientParser.unitDao = unitDao  // because it's lateinit, and it needs resetting
        IngredientParser.units.let {
            it.clear()
            it.addAll(unitsList)
        }

        every { unitDao.findAll() } returns unitsList
        every { applicationContext.getBean(UnitDao::class.java) }  returns unitDao
    }

    @AfterEach
    fun teardown() {
        teardownContext()
        confirmVerified(unitDao)
    }

    @Test
    fun findAmount() {
        assertAll(
            { assertEquals(3.5, parser1.findAmount()) },
            { assertEquals(3.5, parser2.findAmount()) },
            { assertEquals(1.0, parser3.findAmount()) },
            { assertEquals(6.0, parser4.findAmount()) },
            { assertEquals(1.25, parser5.findAmount()) },
        )
    }

    @Test
    fun findUnit() {
        assertAll(
            { assertEquals(unitsList[1], parser1.findUnit()) },
            { assertEquals(unitsList[1], parser2.findUnit()) },
            { assertEquals(unitsList[3], parser3.findUnit()) },
            { assertNull( parser4.findUnit()) },
            { assertEquals(unitsList[4], parser6.findUnit()) },
            { assertEquals(unitsList[0], parser7.findUnit()) },
            { assertEquals(unitsList[6], parser8.findUnit()) },
        )
    }

    @Test
    fun findIngredient() {
        assertAll(
            { assertEquals("flour", parser1.findIngredient()) },
            { assertEquals("sugar", parser2.findIngredient()) },
            { assertEquals("butter", parser3.findIngredient()) },
            { assertEquals("eggs", parser4.findIngredient()) },
            { assertEquals("powdered sugar", parser5.findIngredient()) },
        )

        assertAll(
            { assertTrue(parser1.isIngredient()) },
            { assertTrue(parser2.isIngredient()) },
            { assertTrue(parser3.isIngredient()) },
            { assertTrue(parser4.isIngredient()) },
            { assertTrue(parser5.isIngredient()) },
            { assertTrue(parser6.isIngredient()) },
            { assertTrue(parser7.isIngredient()) },
            { assertFalse(notIngredParser.isIngredient()) },
        )
    }

    @Test
    fun `split tests`() {
        val in9 = "1 c. sugar 1 c. flour"
        val in10 = "1 c. sugar 1 1/2 c. flour"
        val in11 = "1 c. sugar 1 egg"
        val in12 = "1 c. sugar 1 c."

        val parser9 = IngredientParser(in9)
        parser9.findIngredient()
        val parser10 = IngredientParser(in10)
        parser10.findIngredient()
        val parser11 = IngredientParser(in11)
        parser11.findIngredient()
        val parser12 = IngredientParser(in12)
        parser12.findIngredient()

        assertAll(
            { assertEquals(3, parser9.findSplit()) },
            { assertEquals(3, parser10.findSplit()) },
            { assertEquals(3, parser11.findSplit()) },
            { assertEquals(-1, parser12.findSplit()) },
        )
    }

    @Test
    fun `edge cases`() {
        assertAll(
            { assertEquals(0.0, emptyParser.findAmount()) },
            { assertEquals(0.0, emptyParser.amount) },
            { assertNull(emptyParser.findUnit()) },
            { assertNull(emptyParser.unit) },
            { assertEquals("", emptyParser.findIngredient()) },
            { assertEquals("", emptyParser.item) },
            { assertNull(IngredientParser("45 ").findUnit()) },
            { assertNull(IngredientParser.matchesUnit("   ")) },
            { assertNull(IngredientParser.matchesUnit("foo")) },
            { assertEquals(unitsList[1], IngredientParser.matchesUnit("c.")) },
            { assertEquals(unitsList[4], IngredientParser.matchesUnit("fl.oz.")) },
            { assertEquals(unitsList[3], IngredientParser.matchesUnit("pounds")) },
            { assertEquals(unitsList[5], IngredientParser.matchesUnit("tsp")) },
            { assertEquals(unitsList[5], IngredientParser.matchesUnit("tsp.s")) },
            { assertEquals(unitsList[6], IngredientParser.matchesUnit("T.")) },
            { assertEquals(unitsList[5], IngredientParser.matchesUnit("tsp.")) },
            { assertEquals(unitsList[6], IngredientParser.matchesUnit("Ts.")) },
            { assertEquals(unitsList[6], IngredientParser.matchesUnit("T.s")) },
            { assertEquals(unitsList[6], IngredientParser.matchesUnit("T.(s)")) },
            { assertEquals(unitsList[6], IngredientParser.matchesUnit("T")) },
            { assertEquals(unitsList[6], IngredientParser.matchesUnit("Ts")) },
            { assertEquals(unitsList[6], IngredientParser.matchesUnit("tbsp.")) },
            { assertEquals(unitsList[6], IngredientParser.matchesUnit("tbsps.")) },
            { assertEquals(unitsList[6], IngredientParser.matchesUnit("tbsp")) },
            { assertEquals(unitsList[6], IngredientParser.matchesUnit("tbsps")) },
            { assertEquals(unitsList[0], IngredientParser.matchesUnit("mls")) },
            { assertEquals(unitsList[3], IngredientParser.matchesUnit("lbs.")) },
        )
    }

    companion object {
        val unitsList = listOf(
            AUnit(1, "milliliter", UnitType.VOLUME, 1.0, true, "ml"),
            AUnit(2, "cup", UnitType.VOLUME, 236.5882365, false, "c."),
            AUnit(3, "gram", UnitType.WEIGHT, 1.0, true, "g"),
            AUnit(4, "pound", UnitType.WEIGHT, 454.0, false, "lb."),
            AUnit(5, "fluid ounce", UnitType.VOLUME, 29.5735, false, "fl.oz."),
            AUnit(6, "teaspoon", UnitType.VOLUME, 4.75, false, "tsp."),
            AUnit(7, "tablespoon", UnitType.VOLUME, 14.287, false, "tbsp.", "T."),
        )
    }
}