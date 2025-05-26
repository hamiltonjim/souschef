package xyz.jimh.souschef.display

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.Optional
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import xyz.jimh.souschef.ControllerTestBase
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.config.SpringContext
import xyz.jimh.souschef.config.UnitAbbrev
import xyz.jimh.souschef.config.UnitType
import xyz.jimh.souschef.data.AUnit
import xyz.jimh.souschef.data.Preference
import xyz.jimh.souschef.data.PreferenceDao
import xyz.jimh.souschef.data.UnitDao
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

class IngredientFormatterTest : ControllerTestBase() {

    private lateinit var unitDao: UnitDao

    private lateinit var formatter: IngredientFormatter

    @BeforeEach
    fun init() {
        setupContext()
        unitDao = mockk()

        formatter = IngredientFormatter(unitDao)
    }

    @AfterEach
    fun cleanUp() {
        confirmVerified(unitDao)
        teardownContext()
    }

    @Test
    fun `write Unit as full name`() {
        val preferenceDao = mockk<PreferenceDao>()
        Preferences.preferenceDao = preferenceDao
        every { SpringContext.getBean(PreferenceDao::class.java) } returns preferenceDao

        val pref = Preference(HOST, "unitNames", UnitAbbrev.FULL_NAME.name)
        every { preferenceDao.findByHostAndKey(HOST, "unitNames") } returns Optional.of(pref)

        val stringSlot = slot<String>()
        every { unitDao.findByName(capture(stringSlot)) } answers {
            unitByAnyName(stringSlot.captured, UnitAbbrev.FULL_NAME)
        }
        every { unitDao.findByAbbrev(capture(stringSlot)) } answers {
            unitByAnyName(stringSlot.captured, UnitAbbrev.ABBREVIATION)
        }

        Assertions.assertAll(
            { assertEquals("cup", formatter.writeUnit(HOST, "cup")) },
            { assertEquals("cup", formatter.writeUnit(HOST, "c.")) },
            { assertEquals("liter", formatter.writeUnit(HOST, "liter")) },
            { assertEquals("liter", formatter.writeUnit(HOST, "l")) },
            { assertEquals("gallon", formatter.writeUnit(HOST, "gallon")) },
            { assertEquals("gallon", formatter.writeUnit(HOST, "gal.")) },
            { assertEquals("teaspoon", formatter.writeUnit(HOST, "teaspoon")) },
            { assertEquals("teaspoon", formatter.writeUnit(HOST, "tsp.")) },
            { assertEquals("tablespoon", formatter.writeUnit(HOST, "tablespoon")) },
            { assertEquals("tablespoon", formatter.writeUnit(HOST, "tbsp.")) },
            { assertEquals("firkin", formatter.writeUnit(HOST, "firkin")) },
            { assertEquals("firkin", formatter.writeUnit(HOST, "fk")) },
        )

        verify(exactly = 12) { preferenceDao.findByHostAndKey(HOST, "unitNames") }
        verify(exactly = 12) { unitDao.findByName(allAny()) }
        verify(exactly = 6) { unitDao.findByAbbrev(allAny()) }
        confirmVerified(unitDao, preferenceDao)
    }

    @Test
    fun `prefer abbrev but still write full name`() {
        val preferenceDao = mockk<PreferenceDao>()
        Preferences.preferenceDao = preferenceDao
        every { SpringContext.getBean(PreferenceDao::class.java) } returns preferenceDao

        val pref = Preference(HOST, "unitNames", UnitAbbrev.ABBREVIATION.name)
        every { preferenceDao.findByHostAndKey(HOST, "unitNames") } returns Optional.of(pref)

        val stringSlot = slot<String>()
        every { unitDao.findByAbbrev(capture(stringSlot)) } answers {
            unAbbrevUnitList.firstOrNull { it.name == stringSlot.captured }
        }
        every { unitDao.findByAltAbbrev(capture(stringSlot)) } answers {
            unAbbrevUnitList.firstOrNull { it.name == stringSlot.captured }
        }
        every { unitDao.findByName(capture(stringSlot)) } answers {
            unAbbrevUnitList.firstOrNull { it.name == stringSlot.captured }
        }

        Assertions.assertAll(
            { assertEquals("cup", formatter.writeUnit(HOST, "cup")) },
            { assertEquals("liter", formatter.writeUnit(HOST, "liter")) },
            { assertEquals("gallon", formatter.writeUnit(HOST, "gallon")) },
            { assertEquals("teaspoon", formatter.writeUnit(HOST, "teaspoon")) },
            { assertEquals("tablespoon", formatter.writeUnit(HOST, "tablespoon")) },
            { assertEquals("firkin", formatter.writeUnit(HOST, "firkin")) },
            { assertEquals("", formatter.writeUnit(HOST, "unknown")) },
        )

        verify(exactly = 6) { preferenceDao.findByHostAndKey(HOST, "unitNames") }
        verify(exactly = 7) { unitDao.findByName(allAny()) }
        verify(exactly = 1) { unitDao.findByAbbrev("unknown") }
        verify(exactly = 1) { unitDao.findByAltAbbrev("unknown") }
        confirmVerified(unitDao, preferenceDao)
    }

    @Test
    fun `write Unit as abbreviation`() {
        val preferenceDao = mockk<PreferenceDao>()
        Preferences.preferenceDao = preferenceDao
        every { SpringContext.getBean(PreferenceDao::class.java) } returns preferenceDao

        val pref = Preference(HOST, "unitNames", UnitAbbrev.ABBREVIATION.name)
        every { preferenceDao.findByHostAndKey(HOST, "unitNames") } returns Optional.of(pref)

        val stringSlot = slot<String>()
        every { unitDao.findByName(capture(stringSlot)) } answers {
            unitByAnyName(stringSlot.captured, UnitAbbrev.FULL_NAME)
        }
        every { unitDao.findByAbbrev(capture(stringSlot)) } answers {
            unitByAnyName(stringSlot.captured, UnitAbbrev.ABBREVIATION)
        }
        every { unitDao.findByAltAbbrev(capture(stringSlot)) } answers {
            unitByAnyName(stringSlot.captured, UnitAbbrev.ABBREVIATION)
        }

        Assertions.assertAll(
            { assertEquals("c.", formatter.writeUnit(HOST, "cup")) },
            { assertEquals("c.", formatter.writeUnit(HOST, "c.")) },
            { assertEquals("l", formatter.writeUnit(HOST, "liter")) },
            { assertEquals("l", formatter.writeUnit(HOST, "l")) },
            { assertEquals("gal.", formatter.writeUnit(HOST, "gallon")) },
            { assertEquals("gal.", formatter.writeUnit(HOST, "gal.")) },
            { assertEquals("tsp.", formatter.writeUnit(HOST, "teaspoon")) },
            { assertEquals("tsp.", formatter.writeUnit(HOST, "tsp.")) },
            { assertEquals("tbsp.", formatter.writeUnit(HOST, "tablespoon")) },
            { assertEquals("tbsp.", formatter.writeUnit(HOST, "tbsp.")) },
            { assertEquals("tbsp.", formatter.writeUnit(HOST, "T.")) },
            { assertEquals("fk", formatter.writeUnit(HOST, "firkin")) },
            { assertEquals("fk", formatter.writeUnit(HOST, "fk")) },
            { assertEquals("", formatter.writeUnit(HOST, "unknown")) },
        )

        verify { preferenceDao.findByHostAndKey(HOST, "unitNames") }
        verify {
            unitDao.findByName(allAny())
            unitDao.findByAbbrev(allAny())
            unitDao.findByAltAbbrev(allAny())
        }
        confirmVerified(unitDao, preferenceDao)
    }

    private fun unitByAnyName(name: String, type: UnitAbbrev): AUnit? {
        return when (type) {
            UnitAbbrev.FULL_NAME -> unitList.firstOrNull { it.name == name }
            UnitAbbrev.ABBREVIATION -> {
                unitList.firstOrNull { it.abbrev == name } ?: unitList.firstOrNull { it.altAbbrev == name }
            }
        }
    }

    @Test
    fun `write unknown unit name or unit abbrev only`() {
        every { unitDao.findByName("unknown") } returns null
        every { unitDao.findByAbbrev("unknown") } returns null
        every { unitDao.findByAltAbbrev("unknown") } returns null

        val unit = formatter.writeUnit(HOST, "unknown")
        assertEquals("", unit)

        verify {
            unitDao.findByName(allAny())
            unitDao.findByAbbrev(allAny())
            unitDao.findByAltAbbrev(allAny())
        }
    }

    @Test
    fun `write number with fractions`() {
        Assertions.assertAll(
            { assertEquals("0", formatter.writeNumber(0.005), "near 0 to '0'") },
            { assertEquals("0", formatter.writeNumber(0.0), "0 to '0'") },
            { assertEquals(ST_ONE_HALF, formatter.writeNumber(0.5), "0.5 to '1/2'") },

            { assertEquals("1$ST_ONE_QUARTER", formatter.writeNumber(1.25)) },
            { assertEquals("1$ST_THREE_QUARTERS", formatter.writeNumber(1.75000001)) },

            { assertEquals("2$ST_ONE_EIGHTH", formatter.writeNumber(2.125)) },
            { assertEquals("2$ST_THREE_EIGHTHS", formatter.writeNumber(2.375)) },
            { assertEquals(ST_FIVE_EIGHTHS, formatter.writeNumber(0.62494), "near 5/8") },
            { assertEquals(ST_SEVEN_EIGHTHS, formatter.writeNumber(0.875), "seven-eights") },

            { assertEquals("3$ST_ONE_THIRD", formatter.writeNumber(3.333)) },
            { assertEquals("6$ST_TWO_THIRDS", formatter.writeNumber(6.666)) },

            { assertEquals("3$ST_ONE_FIFTH", formatter.writeNumber(3.2)) },
            { assertEquals("6$ST_TWO_FIFTHS", formatter.writeNumber(6.4)) },
            { assertEquals(ST_THREE_FIFTHS, formatter.writeNumber(0.6)) },
            { assertEquals(ST_FOUR_FIFTHS, formatter.writeNumber(0.8)) },

            { assertEquals(ST_ONE_SIXTH, formatter.writeNumber(0.1666)) },
            { assertEquals(ST_FIVE_SIXTHS, formatter.writeNumber(0.8333)) },

            { assertEquals("3.55", formatter.writeNumber(3.55)) }
        )
    }

    @Test
    fun writePlainNumberTest() {
        Assertions.assertAll(
            { assertEquals("", formatter.writePlainNumber(0.0), "0 to ''") },
            { assertEquals("1.234", formatter.writePlainNumber(1.234), "1.234") },
        )
    }

    companion object {
        val unitList = listOf(
            AUnit(10, "firkin", UnitType.VOLUME, 34068.706056, false,"fk"),
            AUnit(5, "liter", UnitType.VOLUME, 1000.0, true, "l"),
            AUnit(4, "gallon", UnitType.VOLUME, 3785.411784, false, "gal."),
            AUnit(6, "fluid ounce", UnitType.VOLUME, 29.57352956, false, "fl.oz."),
            AUnit(3, "quart", UnitType.VOLUME, 946.352946, false, "qt."),
            AUnit(9, "milliliter", UnitType.VOLUME, 1.0, true, "ml"),
            AUnit(8, "teaspoon", UnitType.VOLUME, 4.92892159, false, "tsp."),
            AUnit(2, "pint", UnitType.VOLUME, 473.176473, false, "pt."),
            AUnit(1, "cup", UnitType.VOLUME, 236.5882365, false, "c."),
            AUnit(7, "tablespoon", UnitType.VOLUME, 14.78676478, false, "tbsp.", "T."),

            AUnit(7, "slug", UnitType.WEIGHT, 14593.90293721, false, ),
            AUnit(3, "kilogram", UnitType.WEIGHT, 1000.0, true, "kg"),
            AUnit(6, "gram", UnitType.WEIGHT, 1.0, true, "g"),
            AUnit(1, "ounce", UnitType.WEIGHT, 28.34952312, false, "oz."),
            AUnit(5, "stone", UnitType.WEIGHT, 6350.29318, false, "st."),
            AUnit(2, "pound", UnitType.WEIGHT, 453.59237, false, "lb."),
            AUnit(4, "dram", UnitType.WEIGHT, 1.7718452, false, ),
        )

        val unAbbrevUnitList = unitList.map { AUnit(it.id, it.name, it.type, it.inBase, it.intl) }

        const val HOST = "localhost"
    }
}