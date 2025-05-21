package xyz.jimh.souschef.display

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.Optional
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.function.Executable
import xyz.jimh.souschef.ControllerTestBase
import xyz.jimh.souschef.config.Preferences.preferenceDao
import xyz.jimh.souschef.config.UnitPreference
import xyz.jimh.souschef.config.UnitType
import xyz.jimh.souschef.config.resetLateInitField
import xyz.jimh.souschef.control.UnitController
import xyz.jimh.souschef.data.AUnit
import xyz.jimh.souschef.data.Category
import xyz.jimh.souschef.data.CategoryDao
import xyz.jimh.souschef.data.Preference
import xyz.jimh.souschef.data.PreferenceDao
import xyz.jimh.souschef.data.UnitDao
import xyz.jimh.souschef.data.Volume
import xyz.jimh.souschef.data.Weight

class IngredientBuilderTest : ControllerTestBase() {

    private lateinit var unitController: UnitController
    private lateinit var unitDao: UnitDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var ingredientFormatter: IngredientFormatter

    private val builder = IngredientBuilder

    @BeforeEach
    fun setUp() {
        setupContext()

        resetLateInitField(IngredientBuilder, "categoryDao")
        resetLateInitField(IngredientBuilder, "unitController")
        resetLateInitField(IngredientBuilder, "ingredientFormatter")

        unitController = mockk()
        every { applicationContext.getBean(UnitController::class.java) } returns unitController
        unitDao = mockk()
        ingredientFormatter = IngredientFormatter(unitDao)
        every { applicationContext.getBean(IngredientFormatter::class.java) } returns ingredientFormatter
        every { applicationContext.getBean(PreferenceDao::class.java) } returns preferenceDao

        request = mockk()
        every { request.remoteHost } returns "localhost"
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(unitController, unitDao, preferenceDao)
        clearAllMocks()
    }

    @Test
    fun `build category selector without a categoryDao`() {
        categoryDao = mockk()
        every { applicationContext.getBean(CategoryDao::class.java) } returns categoryDao

        // simulate uninitialized property
        every { categoryDao.findAllByIdNotNullOrderByName() } throws UninitializedPropertyAccessException("CategoryDao")

        assertThrows<UninitializedPropertyAccessException> { builder.buildCategorySelector("foo", "") }

        verify { categoryDao.findAllByIdNotNullOrderByName() }
    }

    @Test
    fun buildCategorySelector() {
        categoryDao = mockk()
        every { applicationContext.getBean(CategoryDao::class.java) } returns categoryDao

        every { categoryDao.findAllByIdNotNullOrderByName() } returns categories
        val noneSelected = builder.buildCategorySelector("foo", "")
        val appsSelected = builder.buildCategorySelector("bar", "Appetizers")
        val breadsSelected = builder.buildCategorySelector("baz", "Breads")
        val fakeSelected = builder.buildCategorySelector("baz", "Fake")
        assertAll(
            Executable { assertTrue(noneSelected.contains("<option value='' selected")) },
            Executable { assertFalse(fakeSelected.contains("selected")) },
            Executable { assertTrue(appsSelected.contains("<option value='Appetizers' selected='true'>")) },
            Executable { assertTrue(breadsSelected.contains("<option value='Breads' selected='true'>")) },
        )

        verify(exactly = 4) { categoryDao.findAllByIdNotNullOrderByName() }
        confirmVerified(categoryDao)
    }

    @Test
    fun buildUnitSelector() {
        every { unitDao.findAllByType(UnitType.WEIGHT) } returns unitList.filter { it.type == UnitType.WEIGHT }
        every { unitDao.findAllByType(UnitType.VOLUME) } returns unitList.filter { it.type == UnitType.VOLUME }
        every { unitDao.findAllByTypeAndIntlFalse(UnitType.VOLUME) } returns listOf(unitList[1])
        every { unitDao.findAllByTypeAndIntlFalse(UnitType.WEIGHT) } returns listOf(unitList[3])
        every { unitDao.findAllByTypeAndIntlTrue(UnitType.VOLUME) } returns listOf(unitList[0])
        every { unitDao.findAllByTypeAndIntlTrue(UnitType.WEIGHT) } returns listOf(unitList[2], unitList[4])

        val unitPreferenceSlot = slot<UnitPreference>()
        every { unitController.getVolumesAscending(capture(unitPreferenceSlot)) } answers {
            val partial = unitList.filter { it.type == UnitType.VOLUME }
            when (unitPreferenceSlot.captured) {
                UnitPreference.ANY -> partial.map { Volume(it) }
                UnitPreference.ENGLISH -> partial.filter { !it.intl }.map { Volume(it) }
                UnitPreference.INTERNATIONAL -> partial.filter { it.intl }.map { Volume(it) }
            }
        }
        every { unitController.getWeightsAscending(capture(unitPreferenceSlot)) } answers {
            val partial = unitList.filter { it.type == UnitType.WEIGHT }
            when (unitPreferenceSlot.captured) {
                UnitPreference.ANY -> partial.map { Weight(it) }
                UnitPreference.ENGLISH -> partial.filter { !it.intl }.map { Weight(it) }
                UnitPreference.INTERNATIONAL -> partial.filter { it.intl }.map { Weight(it) }
            }
        }

        val unitPrefs = listOf(
            Optional.of(Preference("localhost", "units", "english")),
            Optional.of(Preference("localhost", "units", "international")),
            Optional.of(Preference("localhost", "units", "any")),
        )
        var counter = 0
        every { preferenceDao.findByHostAndKey("localhost", "units") } answers {
            unitPrefs[counter++ % unitPrefs.size]
        }

        val cups = builder.buildUnitSelector("localhost", "foo", "cup")
        val ml = builder.buildUnitSelector("localhost", "foo", "milliliter")
        val pound = builder.buildUnitSelector("localhost", "foo", "pound")

        val none = builder.buildUnitSelector("localhost", "foo", "")
        val mg = builder.buildUnitSelector("localhost", "foo", "milligram")
        assertAll(
            Executable { assertFalse(none.contains("selected='true")) },
            Executable { assertTrue(cups.contains("value='cup' selected='true'")) },
            Executable { assertTrue(ml.contains("value='milliliter' selected='true'")) },
            Executable { assertTrue(pound.contains("value='pound' selected='true'")) },
            Executable { assertTrue(mg.contains("value='milligram' selected='true'")) },
        )

        verify {
            unitController.getVolumesAscending(allAny())
            unitController.getWeightsAscending(allAny())
            preferenceDao.findByHostAndKey("localhost", "units")
        }
    }

    @Test
    fun buildAmountInput() {
        val zero = builder.buildAmountInput("foo", 0.0)
        val one = builder.buildAmountInput("foo", 1.0)
        val half = builder.buildAmountInput("foo", 0.5)
        val oneAndAHalf = builder.buildAmountInput("foo", 1.5)
        assertAll(
            Executable { assertTrue(zero.contains("value=''")) },
            Executable { assertTrue(one.contains("value='1.0'")) },
            Executable { assertTrue(half.contains("value='0.5'")) },
            Executable { assertTrue(oneAndAHalf.contains("value='1.5'")) },
        )
    }

    @Test
    fun buildIngredientInput() {
        val something = builder.buildIngredientInput("foo", "something")
        val nothing = builder.buildIngredientInput("foo", "")
        assertAll(
            Executable { assertTrue(something.contains("value='something'")) },
            Executable { assertTrue(nothing.contains("value=''")) },
        )
    }

    @Test
    fun buildDeleteIngredient() {
        val del = builder.buildDeleteIngredient("foo")
        assertTrue(del.contains("onclick='deleteTableRow(this, \"foo\")"))
    }

    companion object {
        val categories = listOf(
            Category("Appetizers"),
            Category("Breads"),
            Category("Desserts"),
            Category("Entrées"),
        )

        val unitList = listOf(
            AUnit(1, "milliliter", UnitType.VOLUME, 1.0, true, "ml"),
            AUnit(2, "cup", UnitType.VOLUME, 236.5882365, false, "c."),
            AUnit(3, "gram", UnitType.WEIGHT, 1.0, true, "g"),
            AUnit(4, "pound", UnitType.WEIGHT, 454.0, false, "lb."),
            AUnit(4, "milligram", UnitType.WEIGHT, 1.0e-3, true, "T"),
        )

    }
}