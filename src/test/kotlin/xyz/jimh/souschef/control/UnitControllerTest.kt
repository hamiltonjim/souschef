package xyz.jimh.souschef.control

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import xyz.jimh.souschef.config.UnitPreference
import xyz.jimh.souschef.config.UnitType
import xyz.jimh.souschef.data.AUnit
import xyz.jimh.souschef.data.UnitDao
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
        every { unitDao.findByAltAbbrev(capture(stringSlot)) } answers
                { unitList.firstOrNull { it.altAbbrev == stringSlot.captured } }

        assertAll(
            { assertEquals(unitList[0], controller.getUnit("cup"), "cup") },
            { assertEquals(unitList[0], controller.getUnit("c."), "c.") },

            { assertEquals(unitList[1], controller.getUnit("pint"), "pint") },
            { assertEquals(unitList[1], controller.getUnit("pt."), "pt.") },

            { assertEquals(unitList[2], controller.getUnit("quart"), "quart") },
            { assertEquals(unitList[2], controller.getUnit("qt."), "qt.") },

            { assertEquals(unitList[3], controller.getUnit("gallon"), "gallon") },
            { assertEquals(unitList[3], controller.getUnit("gal."), "gal.") },

            { assertEquals(unitList[4], controller.getUnit("liter"), "liter") },
            { assertEquals(unitList[4], controller.getUnit("l"), "l") },

            { assertEquals(unitList[5], controller.getUnit("fluid ounce"), "fluid ounce") },
            { assertEquals(unitList[5], controller.getUnit("fl.oz."), "fl.oz.") },

            { assertEquals(unitList[6], controller.getUnit("tablespoon"), "tablespoon") },
            { assertEquals(unitList[6], controller.getUnit("tbsp."), "tbsp.") },
            { assertEquals(unitList[6], controller.getUnit("T."), "T.") },

            { assertEquals(unitList[7], controller.getUnit("teaspoon"), "teaspoon") },
            { assertEquals(unitList[7], controller.getUnit("tsp."), "tsp.") },

            { assertEquals(unitList[8], controller.getUnit("milliliter"), "milliliter") },
            { assertEquals(unitList[8], controller.getUnit("ml"), "ml") },

            { assertEquals(unitList[9], controller.getUnit("firkin"), "firkin")},
            { assertEquals(unitList[9], controller.getUnit("fk"), "fk")},

            { assertEquals(unitList[10], controller.getUnit("ounce"), "ounce") },
            { assertEquals(unitList[10], controller.getUnit("oz."), "oz.") },

            { assertEquals(unitList[11], controller.getUnit("pound"), "pound") },
            { assertEquals(unitList[11], controller.getUnit("lb."), "lb.") },

            { assertEquals(unitList[12], controller.getUnit("kilogram"), "kilogram") },
            { assertEquals(unitList[12], controller.getUnit("kg"), "kg") },

            { assertEquals(unitList[13], controller.getUnit("dram"), "dram") },

            { assertEquals(unitList[14], controller.getUnit("stone"), "stone") },
            { assertEquals(unitList[14], controller.getUnit("st."), "st.") },

            { assertEquals(unitList[15], controller.getUnit("gram"), "gram") },
            { assertEquals(unitList[15], controller.getUnit("g"), "g") },

            { assertNull(controller.getUnit("fake"), "fake") },
        )

        verify(atLeast = 1) {
            unitDao.findByName(allAny())
            unitDao.findByAbbrev(allAny())
            unitDao.findByAltAbbrev(allAny())
        }
    }

    @Test
    fun `get volume by id`() {
        val longSlot = slot<Long>()
        every { volumeDao.findById(capture(longSlot)) } answers {
            val unit = unitList.filter { it.type == UnitType.VOLUME }.firstOrNull {it.id == longSlot.captured}
            val volume = if (unit == null) null else Volume(unit)
            Optional.ofNullable(volume)
        }

        assertAll(
            { assertEquals(Volume(unitList[0]), controller.getVolume(1).get()) },
            { assertEquals(Volume(unitList[1]), controller.getVolume(2).get()) },
            { assertEquals(Volume(unitList[2]), controller.getVolume(3).get()) },
            { assertEquals(Volume(unitList[3]), controller.getVolume(4).get()) },
        )

        verify { volumeDao.findById(allAny()) }
    }

    @Test
    fun `get weight by id`() {
        val longSlot = slot<Long>()
        every { weightDao.findById(capture(longSlot)) } answers {
            val unit = unitList.filter { it.type == UnitType.WEIGHT }.firstOrNull {it.id == longSlot.captured}
            val weight = if (unit == null) null else Weight(unit)
            Optional.ofNullable(weight)
        }

        assertAll(
            { assertEquals(Weight(unitList[10]), controller.getWeight(1).get()) },
            { assertEquals(Weight(unitList[11]), controller.getWeight(2).get()) },
            { assertEquals(Weight(unitList[12]), controller.getWeight(3).get()) },
            { assertEquals(Weight(unitList[13]), controller.getWeight(4).get()) },
        )

        verify { weightDao.findById(allAny()) }
    }

    @Test
    fun updateWeight_test() {
        val longSlot = slot<Long>()
        every { weightDao.findById(capture(longSlot)) } answers {
            val unit = unitList.filter { it.type == UnitType.WEIGHT }.firstOrNull { it.id == longSlot.captured }
            val weight = if (unit == null) null else Weight(unit)
            Optional.ofNullable(weight)
        }

        val weightSlot = slot<Weight>()
        every { weightDao.save(capture(weightSlot)) } answers { weightSlot.captured }

        // a weight with a null ID -- won't be found, so can't update
        val unsavedWeight = Weight("tonne", 1_000_000.0, true, "T")
        assertThrows<IllegalArgumentException> { controller.updateWeight(unsavedWeight) }

        unsavedWeight.id = 12345    // fake ID, should get a different exception
        assertThrows<IllegalStateException> { controller.updateWeight(unsavedWeight) }

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
        assertThrows<IllegalArgumentException> { controller.updateVolume(unsavedVolume) }

        unsavedVolume.id = 12345    // fake ID, should get a different exception
        assertThrows<IllegalStateException> { controller.updateVolume(unsavedVolume) }

        verify(exactly = 1) { volumeDao.findById(12345) }

        val realVolume = Volume(unitList[4])   // gallon
        realVolume.name = "galleon"

        assertEquals(realVolume, controller.updateVolume(realVolume), "update failed")
        verify(exactly = 1) {
            volumeDao.findById(realVolume.id!!)
            volumeDao.save(realVolume)
        }
    }

    @Test
    fun `get volumes ascending test`() {
        every { volumeDao.findAllByInBaseGreaterThanOrderByInBase(0.0) } answers {
            unitList.filter { it.type == UnitType.VOLUME }
                .map { Volume(it) }
                .sortedBy { it.inBase }
        }
        every { volumeDao.findAllByIntlIsFalseOrderByInBase() } answers {
            unitList.filter { it.type == UnitType.VOLUME }
                .filter { !it.intl }
                .map { Volume(it) }
                .sortedBy { it.inBase }
        }
        every { volumeDao.findAllByIntlIsTrueOrderByInBase() } answers {
            unitList.filter { it.type == UnitType.VOLUME }
                .filter { it.intl }
                .map { Volume(it) }
                .sortedBy { it.inBase }
        }
        val all = controller.getVolumesAscending(UnitPreference.ANY)
        val intl = controller.getVolumesAscending(UnitPreference.INTERNATIONAL)
        val english = controller.getVolumesAscending(UnitPreference.ENGLISH)

        assertAll(
            { assertEquals(10, all.size) },
            { assertEquals(8, english.size) },
            { assertEquals(2, intl.size) },
            { assertEquals(1.0, intl[0].inBase) },
            { assertEquals(1e3, intl[1].inBase) },
            { assertEquals("teaspoon", english[0].name) },
            { assertEquals("firkin", english[7].name) },
        )

        verify {
            volumeDao.findAllByInBaseGreaterThanOrderByInBase(0.0)
            volumeDao.findAllByIntlIsFalseOrderByInBase()
            volumeDao.findAllByIntlIsTrueOrderByInBase()
        }
    }

    @Test
    fun `get weights ascending test`() {
        every { weightDao.findAllByInBaseGreaterThanOrderByInBase(0.0) } answers {
            unitList.filter { it.type == UnitType.WEIGHT }
                .map { Weight(it) }
                .sortedBy { it.inBase }
        }
        every { weightDao.findAllByIntlIsFalseOrderByInBase() } answers {
            unitList.filter { it.type == UnitType.WEIGHT }
                .filter { !it.intl }
                .map { Weight(it) }
                .sortedBy { it.inBase }
        }
        every { weightDao.findAllByIntlIsTrueOrderByInBase() } answers {
            unitList.filter { it.type == UnitType.WEIGHT }
                .filter { it.intl }
                .map { Weight(it) }
                .sortedBy { it.inBase }
        }
        val all = controller.getWeightsAscending(UnitPreference.ANY)
        val intl = controller.getWeightsAscending(UnitPreference.INTERNATIONAL)
        val english = controller.getWeightsAscending(UnitPreference.ENGLISH)

        assertAll(
            { assertEquals(7, all.size) },
            { assertEquals(5, english.size) },
            { assertEquals(2, intl.size) },
            { assertEquals(1.0, intl[0].inBase) },
            { assertEquals(1e3, intl[1].inBase) },
            { assertEquals("dram", english[0].name) },
            { assertEquals("slug", english[4].name) },
        )

        verify {
            weightDao.findAllByInBaseGreaterThanOrderByInBase(0.0)
            weightDao.findAllByIntlIsFalseOrderByInBase()
            weightDao.findAllByIntlIsTrueOrderByInBase()
        }
    }

    @Test
    fun `get all units test`() {
        every { unitDao.findAll() } returns unitList
        assertEquals(unitList, controller.getUnits())
        verify { unitDao.findAll() }
    }

    @Test
    fun `get all volumes test`() {
        every { volumeDao.findAll() } returns volumeList
        assertEquals(volumeList, controller.getVolumes())
        verify { volumeDao.findAll() }
    }

    @Test
    fun `get all weights test`() {
        every { weightDao.findAll() } returns weightList
        assertEquals(weightList, controller.getWeights())
        verify { weightDao.findAll() }
    }

    @Test
    fun `add units test`() {
        val weightCapturingSlot = slot<Weight>()
        every { weightDao.save(capture(weightCapturingSlot)) } answers {
            val unit = weightCapturingSlot.captured
            unit.id = ++id
            unit
        }

        val weight = Weight("shit-ton", 1e12, false)
        controller.addWeight(weight)
        assertEquals(id, weight.id)

        val volumeCapturingSlot = slot<Volume>()
        every { volumeDao.save(capture(volumeCapturingSlot)) } answers {
            val unit = volumeCapturingSlot.captured
            unit.id = ++id
            unit
        }

        val volume = Volume("boat-load", 1e15, false)
        controller.addVolume(volume)
        assertEquals(id, volume.id)

        verify {
            weightDao.save(weight)
            volumeDao.save(volume)
        }
    }

    companion object {
        var id = 100L

        val unitList = listOf(
            AUnit(1, "cup", UnitType.VOLUME, 236.5882365, false, "c."),
            AUnit(2, "pint", UnitType.VOLUME, 473.176473, false, "pt."),
            AUnit(3, "quart", UnitType.VOLUME, 946.352946, false, "qt."),
            AUnit(4, "gallon", UnitType.VOLUME, 3785.411784, false, "gal."),
            AUnit(5, "liter", UnitType.VOLUME, 1000.0, true, "l"),
            AUnit(6, "fluid ounce", UnitType.VOLUME, 29.57352956, false, "fl.oz."),
            AUnit(7, "tablespoon", UnitType.VOLUME, 14.78676478, false, "tbsp.", "T."),
            AUnit(8, "teaspoon", UnitType.VOLUME, 4.92892159, false, "tsp."),
            AUnit(9, "milliliter", UnitType.VOLUME, 1.0, true, "ml"),
            AUnit(10, "firkin", UnitType.VOLUME, 34068.706056, false,"fk"),

            AUnit(1, "ounce", UnitType.WEIGHT, 28.34952312, false, "oz."),
            AUnit(2, "pound", UnitType.WEIGHT, 453.59237, false, "lb."),
            AUnit(3, "kilogram", UnitType.WEIGHT, 1000.0, true, "kg"),
            AUnit(4, "dram", UnitType.WEIGHT, 1.7718452, false ),
            AUnit(5, "stone", UnitType.WEIGHT, 6350.29318, false, "st."),
            AUnit(6, "gram", UnitType.WEIGHT, 1.0, true, "g"),
            AUnit(7, "slug", UnitType.WEIGHT, 14593.90293721, false ),
        )

        val volumeList = unitList.filter { it.type == UnitType.VOLUME }.map { Volume(it) }
        val weightList = unitList.filter { it.type == UnitType.WEIGHT }.map { Weight(it) }
    }
}