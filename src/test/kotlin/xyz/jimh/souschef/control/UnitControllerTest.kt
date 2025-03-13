package xyz.jimh.souschef.control

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import xyz.jimh.souschef.data.AUnit
import xyz.jimh.souschef.data.UnitDao
import xyz.jimh.souschef.data.UnitType
import xyz.jimh.souschef.data.Volume
import xyz.jimh.souschef.data.VolumeDao
import xyz.jimh.souschef.data.Weight
import xyz.jimh.souschef.data.WeightDao

class UnitControllerTest {

    private lateinit var unitDao: UnitDao
    private lateinit var volumeDao: VolumeDao
    private lateinit var weightDao: WeightDao

    private lateinit var controller: UnitController

    @BeforeEach
    fun setUp() {
        unitDao = mockk()
        volumeDao = mockk()
        weightDao = mockk()

        controller = UnitController(unitDao, volumeDao, weightDao)
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(unitDao, volumeDao, weightDao)
    }

    @Test
    fun getUnit_test() {
        val stringSlot = slot<String>()
        every { unitDao.findByName(capture(stringSlot)) } answers
                { unitList.firstOrNull { it.name == stringSlot.captured } }
        every { unitDao.findByAbbrev(capture(stringSlot)) } answers
                { unitList.firstOrNull { it.abbrev == stringSlot.captured } }

        Assertions.assertAll(
            Executable { assertEquals(unitList[0], controller.getUnit("cup"), "cup") },
            Executable { assertEquals(unitList[0], controller.getUnit("c."), "c.") },

            Executable { assertEquals(unitList[1], controller.getUnit("pint"), "pint") },
            Executable { assertEquals(unitList[1], controller.getUnit("pt."), "pt.") },

            Executable { assertEquals(unitList[2], controller.getUnit("quart"), "quart") },
            Executable { assertEquals(unitList[2], controller.getUnit("qt."), "qt.") },

            Executable { assertEquals(unitList[3], controller.getUnit("gallon"), "gallon") },
            Executable { assertEquals(unitList[3], controller.getUnit("gal."), "gal.") },

            Executable { assertEquals(unitList[4], controller.getUnit("liter"), "liter") },
            Executable { assertEquals(unitList[4], controller.getUnit("l"), "l") },

            Executable { assertEquals(unitList[5], controller.getUnit("fluid ounce"), "fluid ounce") },
            Executable { assertEquals(unitList[5], controller.getUnit("fl.oz."), "fl.oz.") },

            Executable { assertEquals(unitList[6], controller.getUnit("tablespoon"), "tablespoon") },
            Executable { assertEquals(unitList[6], controller.getUnit("tbsp."), "tbsp.") },

            Executable { assertEquals(unitList[7], controller.getUnit("teaspoon"), "teaspoon") },
            Executable { assertEquals(unitList[7], controller.getUnit("tsp."), "tsp.") },

            Executable { assertEquals(unitList[8], controller.getUnit("milliliter"), "milliliter") },
            Executable { assertEquals(unitList[8], controller.getUnit("ml"), "ml") },

            Executable { assertEquals(unitList[9], controller.getUnit("firkin"), "firkin")},
            Executable { assertEquals(unitList[9], controller.getUnit("fk"), "fk")},

            Executable { assertEquals(unitList[10], controller.getUnit("ounce"), "ounce") },
            Executable { assertEquals(unitList[10], controller.getUnit("oz."), "oz.") },

            Executable { assertEquals(unitList[11], controller.getUnit("pound"), "pound") },
            Executable { assertEquals(unitList[11], controller.getUnit("lb."), "lb.") },

            Executable { assertEquals(unitList[12], controller.getUnit("kilogram"), "kilogram") },
            Executable { assertEquals(unitList[12], controller.getUnit("kg"), "kg") },

            Executable { assertEquals(unitList[13], controller.getUnit("dram"), "dram") },

            Executable { assertEquals(unitList[14], controller.getUnit("stone"), "stone") },
            Executable { assertEquals(unitList[14], controller.getUnit("st."), "st.") },

            Executable { assertEquals(unitList[15], controller.getUnit("gram"), "gram") },
            Executable { assertEquals(unitList[15], controller.getUnit("g"), "g") },

            Executable { assertNull(controller.getUnit("fake"), "fake") },
        )

        verify(atLeast = 1) {
            unitDao.findByName(allAny())
            unitDao.findByAbbrev(allAny())
        }
    }

    @Test
    fun updateWeight_test() {
        val longSlot = slot<Long>()
        every { weightDao.findById(capture(longSlot)) } answers {
            val unit = unitList.filter { it.type == UnitType.WEIGHT }.firstOrNull { it.id == longSlot.captured }
            val weight = if (unit == null)
                null
            else
                Weight(unit)
            Optional.ofNullable(weight)
        }

        val weightSlot = slot<Weight>()
        every { weightDao.save(capture(weightSlot)) } answers { weightSlot.captured }

        // a weight with a null ID -- won't be found, so can't update
        val unsavedWeight = Weight("tonne", 1_000_000.0, true, "T")
        assertThrows(IllegalArgumentException::class.java) { controller.updateWeight(unsavedWeight) }

        unsavedWeight.id = 12345    // fake ID, should get a different exception
        assertThrows(IllegalStateException::class.java) { controller.updateWeight(unsavedWeight) }

        verify(exactly = 1) { weightDao.findById(12345) }

        val realWeight = Weight(unitList[14])   // stone
        realWeight.abbrev = "sto."

        assertEquals(realWeight, controller.updateWeight(realWeight), "update failed")
        verify(exactly = 1) {
            weightDao.findById(realWeight.id!!)
            weightDao.save(realWeight)
        }
    }

    @Test
    fun updateVolume_test() {
        val longSlot = slot<Long>()
        every { volumeDao.findById(capture(longSlot)) } answers {
            val unit = unitList.filter { it.type == UnitType.WEIGHT }.firstOrNull { it.id == longSlot.captured }
            val volume = if (unit == null)
                null
            else
                Volume(unit)
            Optional.ofNullable(volume)
        }

        val volumeSlot = slot<Volume>()
        every { volumeDao.save(capture(volumeSlot)) } answers { volumeSlot.captured }

        // a volume with a null ID -- won't be found, so can't update
        val unsavedVolume = Volume("tonne", 1_000_000.0, true, "T")
        assertThrows(IllegalArgumentException::class.java) { controller.updateVolume(unsavedVolume) }

        unsavedVolume.id = 12345    // fake ID, should get a different exception
        assertThrows(IllegalStateException::class.java) { controller.updateVolume(unsavedVolume) }

        verify(exactly = 1) { volumeDao.findById(12345) }

        val realVolume = Volume(unitList[4])   // gallon
        realVolume.name = "galleon"

        assertEquals(realVolume, controller.updateVolume(realVolume), "update failed")
        verify(exactly = 1) {
            volumeDao.findById(realVolume.id!!)
            volumeDao.save(realVolume)
        }
    }

    companion object {
        val unitList = listOf(
            AUnit(1, "cup", UnitType.VOLUME, 236.5882365, false, "c."),
            AUnit(2, "pint", UnitType.VOLUME, 473.176473, false, "pt."),
            AUnit(3, "quart", UnitType.VOLUME, 946.352946, false, "qt."),
            AUnit(4, "gallon", UnitType.VOLUME, 3785.411784, false, "gal."),
            AUnit(5, "liter", UnitType.VOLUME, 1000.0, true, "l"),
            AUnit(6, "fluid ounce", UnitType.VOLUME, 29.57352956, false, "fl.oz."),
            AUnit(7, "tablespoon", UnitType.VOLUME, 14.78676478, false, "tbsp."),
            AUnit(8, "teaspoon", UnitType.VOLUME, 4.92892159, false, "tsp."),
            AUnit(9, "milliliter", UnitType.VOLUME, 1.0, true, "ml"),
            AUnit(10, "firkin", UnitType.VOLUME, 34068.706056, false,"fk"),

            AUnit(1, "ounce", UnitType.WEIGHT, 28.34952312, false, "oz."),
            AUnit(2, "pound", UnitType.WEIGHT, 453.59237, false, "lb."),
            AUnit(3, "kilogram", UnitType.WEIGHT, 1000.0, true, "kg"),
            AUnit(4, "dram", UnitType.WEIGHT, 1.7718452, false, ),
            AUnit(5, "stone", UnitType.WEIGHT, 6350.29318, false, "st."),
            AUnit(6, "gram", UnitType.WEIGHT, 1.0, true, "g"),
            AUnit(7, "slug", UnitType.WEIGHT, 14593.90293721, false, ),
        )
    }
}