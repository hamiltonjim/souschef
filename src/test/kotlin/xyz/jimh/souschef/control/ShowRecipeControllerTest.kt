package xyz.jimh.souschef.control

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.Optional
import kotlin.test.assertTrue
import kotlin.test.fail
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import xyz.jimh.souschef.ControllerTestBase
import xyz.jimh.souschef.config.Broadcaster
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.config.SpringContext
import xyz.jimh.souschef.config.UnitAbbrev
import xyz.jimh.souschef.config.UnitPreference
import xyz.jimh.souschef.config.UnitType
import xyz.jimh.souschef.data.AUnit
import xyz.jimh.souschef.data.FoodItem
import xyz.jimh.souschef.data.Ingredient
import xyz.jimh.souschef.data.Preference
import xyz.jimh.souschef.data.PreferenceDao
import xyz.jimh.souschef.data.Recipe
import xyz.jimh.souschef.data.UnitDao
import xyz.jimh.souschef.data.Volume
import xyz.jimh.souschef.data.VolumeDao
import xyz.jimh.souschef.data.Weight
import xyz.jimh.souschef.data.WeightDao
import xyz.jimh.souschef.display.IngredientFormatter

class ShowRecipeControllerTest : ControllerTestBase() {

    private lateinit var foodController: FoodController
    private lateinit var ingredientController: IngredientController
    private lateinit var recipeController: RecipeController
    private lateinit var unitDao: UnitDao
    private lateinit var volumeDao: VolumeDao
    private lateinit var weightDao: WeightDao
    private lateinit var ingredientFormatter: IngredientFormatter

    private lateinit var controller: ShowRecipeController

    private lateinit var preferenceDao: PreferenceDao

    @BeforeEach
    fun setUp() {
        setupContext()

        foodController = mockk()
        ingredientController = mockk()
        recipeController = mockk()
        unitDao = mockk()
        volumeDao = mockk()
        weightDao = mockk()
        ingredientFormatter = IngredientFormatter(unitDao)

        controller = ShowRecipeController(
            foodController,
            ingredientController,
            recipeController,
            unitDao,
            volumeDao,
            weightDao,
            ingredientFormatter
        )

        preferenceDao = mockk()
        Preferences.preferenceDao = preferenceDao
        every { SpringContext.getBean(PreferenceDao::class.java) } returns preferenceDao

        every { preferenceDao.findByHostAndKey(any(), "units") } returns
                Optional.of(Preference("localhost", "units", UnitPreference.ENGLISH.name))
        every { preferenceDao.findByHostAndKey(any(), "unitNames") } returns
                Optional.of(Preference("localhost", "unitNames", UnitAbbrev.FULL_NAME.name))
        every { preferenceDao.findAllByHost("localhost") } returns prefList

        every { recipeController.getRecipe(POUND_CAKE_ID) } returns recipe
        every { recipeController.getRecipe(ALL_ID) } returns recipeWithAllTypesOfIngredients
        every { ingredientController.getIngredientInventory(POUND_CAKE_ID) } returns ingredients
        every { ingredientController.getIngredientInventory(ALL_ID) } returns ingredientsOfAllTypes

        every { unitDao.findAllByType(UnitType.WEIGHT) } returns unitList.filter { it.type == UnitType.WEIGHT }
        every { unitDao.findAllByType(UnitType.VOLUME) } returns unitList.filter { it.type == UnitType.VOLUME }
        every { unitDao.findAllByTypeAndIntlFalse(UnitType.VOLUME) } returns listOf(unitList[1])
        every { unitDao.findAllByTypeAndIntlFalse(UnitType.WEIGHT) } returns listOf(unitList[3])
        every { unitDao.findAllByTypeAndIntlTrue(UnitType.VOLUME) } returns listOf(unitList[0])
        every { unitDao.findAllByTypeAndIntlTrue(UnitType.WEIGHT) } returns listOf(unitList[2], unitList[4])

        val enumSlot = slot<UnitType>()
        every { unitDao.findAllByType(capture(enumSlot)) } answers { unitList.filter { it.type == enumSlot.captured }}
        every { unitDao.findByAnyNameAndType("pound", UnitType.WEIGHT) } returns
                unitList.first { it.name == "pound" }
        every { unitDao.findByName("pound") } returns unitList.first { it.name == "pound" }

        val stringSlot = slot<String>()
        every { volumeDao.findByAnyName(capture(stringSlot)) } answers { volumeList.firstOrNull { it.name == stringSlot.captured } }
        every { weightDao.findByAnyName(capture(stringSlot)) } answers { weightList.firstOrNull { it.name == stringSlot.captured } }

        every { volumeDao.findByAnyName("boat-load") } returns Volume("boat-load", Double.MAX_VALUE, false)
        every { unitDao.findByAnyNameAndType("boat-load", UnitType.VOLUME) } returns null
        every { unitDao.findByName("boat-load") } returns null
        every { unitDao.findByAbbrev("boat-load") } returns null

        val longSlot = slot<Long>()
        every { foodController.getFood(capture(longSlot)) } answers {
            Optional.ofNullable(foodItemList.firstOrNull { it.id == longSlot.captured })
        }
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(
            foodController,
            ingredientController,
            recipeController,
            unitDao,
            volumeDao,
            weightDao,
        )
        clearAllMocks()
    }

    @Test
    fun `check that listener listens`() {
        controller.init()
        Preferences.broadcast("bar", "foo")
        assertEquals("foo" to "bar", controller.lastMessage)
        Preferences.broadcast("baz")
        assertEquals(Broadcaster.NO_NAME to "baz", controller.lastMessage)
        controller.destroy()
    }

    @Test
    fun `test show Recipe with original servings`() {
        val response = controller.showRecipe(request, POUND_CAKE_ID)
        Assertions.assertNotNull(response.body)
        val body = response.body!!

        val executables = mutableListOf(Executable {
            Assertions.assertTrue(body.contains("<tr><td>1</td>"), "Incorrect proportions")
        })
        ingredients.forEach { ingredient ->
            val foodItem = foodItemList.firstOrNull { item -> item.id == ingredient.id }
            if (foodItem != null) {
                val exec = Executable {
                    Assertions.assertTrue(body.contains(foodItem.name), "${foodItem.name} is missing")
                }
                executables.add(exec)
            }
        }

        Assertions.assertAll(executables)

        verify {
            foodController.getFood(allAny())
        }
        verify(exactly = 1) { ingredientController.getIngredientInventory(POUND_CAKE_ID) }
        verify(exactly = 1) { recipeController.getRecipe(POUND_CAKE_ID) }
        verify {
            unitDao.findByAnyNameAndType("pound", UnitType.WEIGHT)
            unitDao.findByAnyNameAndType("boat-load", UnitType.VOLUME)
            unitDao.findAllByTypeAndIntlFalse(UnitType.WEIGHT)
            unitDao.findByName("pound")
            unitDao.findByName("boat-load")
            unitDao.findByAbbrev("boat-load")
        }
        verify(atLeast = 4) { volumeDao.findByAnyName("pound") }
        verify(exactly = 1) { volumeDao.findByAnyName("boat-load") }
        verify(atLeast = 4) { weightDao.findByAnyName("pound") }
    }

    @Test
    fun `test show Recipe with adjusted servings`() {
        val response = controller.showRecipe(request, POUND_CAKE_ID, recipe.servings * 2.5)
        Assertions.assertNotNull(response.body)
        val body = response.body!!

        val executables = mutableListOf(Executable {
            Assertions.assertTrue(body.contains("<tr><td>2½</td>"), "Incorrect proportions")
        })
        ingredients.forEach { ingredient ->
            val foodItem = Optional.ofNullable(foodItemList.firstOrNull { item -> item.id == ingredient.id })
            if (foodItem.isPresent) {
                val food = foodItem.get()
                val exec = Executable {
                    Assertions.assertTrue(body.contains(food.name), "${food.name} is missing")
                }
                executables.add(exec)
            }
        }

        Assertions.assertAll(executables)

        verify {
            foodController.getFood(allAny())
        }
        verify(exactly = 1) { ingredientController.getIngredientInventory(POUND_CAKE_ID) }
        verify(exactly = 1) { recipeController.getRecipe(POUND_CAKE_ID) }
        verify {
            unitDao.findByAnyNameAndType("pound", UnitType.WEIGHT)
            unitDao.findByAnyNameAndType("boat-load", UnitType.VOLUME)
            unitDao.findAllByTypeAndIntlFalse(UnitType.WEIGHT)
            unitDao.findByName("pound")
            unitDao.findByName("boat-load")
            unitDao.findByAbbrev("boat-load")
        }
        verify(exactly = 4) { volumeDao.findByAnyName("pound") }
        verify(exactly = 1) { volumeDao.findByAnyName("boat-load") }
        verify(exactly = 4) { weightDao.findByAnyName("pound") }
        verify { preferenceDao.findAllByHost(allAny()) }
    }

    /**
     * this is a contrived tests, since it "can't happen"
     */
    @Test
    fun `test show Recipe with adjusted servings and null ID`() {
        every { recipeController.getRecipe(idForBrokenRecipe) } returns brokenRecipe
        try {
            controller.showRecipe(request, idForBrokenRecipe, brokenRecipe.servings * 2.5)
            fail("Should have thrown IllegalStateException")
        } catch (_: IllegalStateException) {
        } catch (e: Exception) {
            fail("should have thrown ${e::class.java.simpleName}")
        }

        verify { recipeController.getRecipe(idForBrokenRecipe) }
    }

    @Test
    fun `check last message received`() {
        val oldTime = controller.lastMessageTime

        controller.listen("foo", "bar")
        val newTime = controller.lastMessageTime
        val message = controller.lastMessage
        Assertions.assertAll(
            Executable { assertEquals("foo" to "bar", message, "Last message received from server") },
            Executable { assertTrue(oldTime == null || newTime!! > oldTime, "Last message received from server") }
        )
    }

    @Test
    fun `check unit types`() {
        val stringSlot = slot<String>()
        every { unitDao.findByName(capture(stringSlot)) } answers { unitList.firstOrNull { it.name == stringSlot.captured }}
        every { unitDao.findByAbbrev("thing") } returns null
        every { unitDao.findByAbbrev("") } returns null
        every { unitDao.findByAnyNameAndType("pound", UnitType.VOLUME) } returns null
        every { unitDao.findByAnyNameAndType("cup", UnitType.VOLUME) } returns unitList[1]
        every { unitDao.findByAnyNameAndType("thing", any()) } returns null
        every { unitDao.findAllByTypeAndIntlTrue(UnitType.VOLUME) } returns listOf(unitList[0])
        every { unitDao.findAllByTypeAndIntlTrue(UnitType.WEIGHT) } returns listOf(unitList[2])

        val english = Optional.of(Preference("localhost", "units", UnitPreference.ENGLISH.name))
        val intl = Optional.of(Preference("localhost", "units", UnitPreference.INTERNATIONAL.name))
        val any = Optional.of(Preference("localhost", "units", UnitPreference.ANY.name))

        every { preferenceDao.findByHostAndKey(any(), "units") } returns english
        // English units
        val response = controller.showRecipe(request, ALL_ID)
        Assertions.assertNotNull(response.body)
        var body = response.body!!
        Assertions.assertAll(
            Executable { assertTrue(body.contains("cup")) },
            Executable { assertTrue(body.contains("pound")) },
        )

        every { preferenceDao.findByHostAndKey(any(), "units") } returns intl
        // International units
        val intlResponse = controller.showRecipe(request, ALL_ID)
        Assertions.assertNotNull(intlResponse.body)
        body = intlResponse.body!!
        Assertions.assertAll(
            Executable { assertTrue(body.contains("milliliter")) },
            Executable { assertTrue(body.contains("gram")) },
        )

        every { preferenceDao.findByHostAndKey(any(), "units") } returns any
        // any units
        val anyResponse = controller.showRecipe(request, ALL_ID)
        Assertions.assertNotNull(anyResponse.body)
        body = anyResponse.body!!
        Assertions.assertAll(
            Executable { assertTrue(body.contains("cup")) },
            Executable { assertTrue(body.contains("pound")) },
        )

        verify { weightDao.findByAnyName(allAny()) }
        verify { foodController.getFood(allAny()) }
        verify { ingredientController.getIngredientInventory(allAny()) }
        verify { recipeController.getRecipe(ALL_ID) }
        verify {
            unitDao.findByName(allAny())
            unitDao.findByAbbrev(allAny())
            unitDao.findByAnyNameAndType(allAny(), allAny())
            unitDao.findAllByTypeAndIntlFalse(allAny())
            unitDao.findAllByTypeAndIntlTrue(allAny())
            unitDao.findAllByType(allAny())
            unitDao.findByName(allAny())
            volumeDao.findByAnyName(allAny())
        }

    }

    companion object {
        const val POUND_CAKE_ID = 75L
        const val ALL_ID = 175L

        val prefList = listOf(
            Preference("localhost", "showDeleted", "false"),
            Preference("localhost", "language", "en_US"),
            Preference("localhost", "units", "english"),
            Preference("localhost", "unitNames", "abbreviation")
        )

        val weightList = listOf(
            Weight("gram", 1.0, true, "g"),
            Weight("pound", 454.0, false, "lb."),
        )

        val volumeList = listOf(
            Volume("milliliter", 1.0, true, "ml"),
            Volume("cup", 273.0, false, "c."),
        )

        val unitList = listOf(
            AUnit(1, "milliliter", UnitType.VOLUME, 1.0, true, "ml"),
            AUnit(2, "cup", UnitType.VOLUME, 236.5882365, false, "c."),
            AUnit(3, "gram", UnitType.WEIGHT, 1.0, true, "g"),
            AUnit(4, "pound", UnitType.WEIGHT, 454.0, false, "lb."),
            AUnit(4, "milligram", UnitType.WEIGHT, 1.0e-3, true, "T"),
        )

        val foodItemList = listOf(
            FoodItem("sugar", id = 1),
            FoodItem("flour", id = 2),
            FoodItem("eggs", id = 3),
            FoodItem("butter", id = 4),
        )

        val recipe = Recipe("pound cake", "mix", 4, 4L, POUND_CAKE_ID)
        val brokenRecipe = recipe.copy(id = null)
        val idForBrokenRecipe = 1337L

        val ingredients = listOf(
            Ingredient(1L, 1.0, "pound", POUND_CAKE_ID, 1),
            Ingredient(2L, 1.0, "pound", POUND_CAKE_ID, 2),
            Ingredient(3L, 1.0, "pound", POUND_CAKE_ID, 3),
            Ingredient(4L, 1.0, "pound", POUND_CAKE_ID, 4),
            // ingredient to cover unit type not found
            Ingredient(5L, 1.0, "boat-load", POUND_CAKE_ID, 5),
        )

        val recipeWithAllTypesOfIngredients = Recipe("all", "", 1, 4L, ALL_ID)
        val ingredientsOfAllTypes = listOf(
            Ingredient(1L, 1.0, "cup", ALL_ID, 101),
            Ingredient(2L, 1.0, "thing", ALL_ID, 102),
            Ingredient(3L, 1.0, "pound", ALL_ID, 103),
            Ingredient(4L, 1e-4, "pound", ALL_ID, 104),
            Ingredient(3L, 1.0, null, ALL_ID, 105),
            Ingredient(3L, 1.0, "", ALL_ID, 106),
            Ingredient(3L, 5.0, "cup", ALL_ID, 107),
            Ingredient(99L, 0.01, "cup", ALL_ID, 108),
        )

    }
}