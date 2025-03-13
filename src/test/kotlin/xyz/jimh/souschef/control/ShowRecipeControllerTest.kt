package xyz.jimh.souschef.control

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import xyz.jimh.souschef.ControllerTestBase
import xyz.jimh.souschef.config.SpringContext
import xyz.jimh.souschef.config.UnitAbbrev
import xyz.jimh.souschef.config.UnitPreference
import xyz.jimh.souschef.data.AUnit
import xyz.jimh.souschef.data.FoodItem
import xyz.jimh.souschef.data.Ingredient
import xyz.jimh.souschef.data.Preference
import xyz.jimh.souschef.data.PreferenceDao
import xyz.jimh.souschef.data.Recipe
import xyz.jimh.souschef.data.UnitDao
import xyz.jimh.souschef.data.UnitType
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

    private lateinit var showRecipeController: ShowRecipeController

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

        showRecipeController = ShowRecipeController(
            foodController,
            ingredientController,
            recipeController,
            unitDao,
            volumeDao,
            weightDao,
            ingredientFormatter
        )

        preferenceDao = mockk()
        every { SpringContext.getBean(PreferenceDao::class.java) } returns preferenceDao

        every { preferenceDao.findByHostAndKey(any(), "units") } returns
                Optional.of(Preference("localhost", "units", UnitPreference.ENGLISH.name))
        every { preferenceDao.findByHostAndKey(any(), "unitNames") } returns
                Optional.of(Preference("localhost", "unitNames", UnitAbbrev.FULL_NAME.name))

        every { recipeController.getRecipe(POUND_CAKE_ID) } returns recipe
        every { ingredientController.getIngredientInventory(POUND_CAKE_ID) } returns ingredients

        every { unitDao.findAllByType(UnitType.WEIGHT) } returns unitList.filter { it.type == UnitType.WEIGHT }
        every { unitDao.findAllByType(UnitType.VOLUME) } returns unitList.filter { it.type == UnitType.VOLUME }
        every { unitDao.findAllByTypeAndIntlFalse(UnitType.WEIGHT) } returns unitList.filter { !it.intl }
        every { unitDao.findByAnyNameAndType("pound", UnitType.WEIGHT) } returns
                unitList.first { it.name == "pound" }
        every { unitDao.findByName("pound") } returns unitList.first { it.name == "pound" }

        every { volumeDao.findByAnyName("pound") } returns null
        every { weightDao.findByAnyName("pound") } returns weightList.first { it.name == "pound" }

        val longSlot = slot<Long>()
        every { foodController.getFood(capture(longSlot)) } answers {
            Optional.ofNullable(foodItemList.first { it.id == longSlot.captured })
        }
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test show Recipe with original servings`() {
        val response = showRecipeController.showRecipe(request, POUND_CAKE_ID)
        Assertions.assertNotNull(response.body)
        val body = response.body!!

        val executables = mutableListOf(Executable {
            Assertions.assertTrue(body.contains("<tr><td>1</td>"), "Incorrect proportions")
        })
        ingredients.forEach { ingredient ->
            val foodItem = foodItemList.first { item -> item.id == ingredient.id }
            val exec = Executable {
                Assertions.assertTrue(body.contains(foodItem.name), "${foodItem.name} is missing")
            }
            executables.add(exec)
        }

        Assertions.assertAll(executables)

        verify(exactly = 1) {
            foodController.getFood(1L)
            foodController.getFood(2L)
            foodController.getFood(3L)
            foodController.getFood(4L)
        }
        verify(exactly = 1) { ingredientController.getIngredientInventory(POUND_CAKE_ID) }
        verify(exactly = 1) { recipeController.getRecipe(POUND_CAKE_ID) }
        verify(exactly = 4) {
            unitDao.findByAnyNameAndType("pound", UnitType.WEIGHT)
            unitDao.findAllByTypeAndIntlFalse(UnitType.WEIGHT)
            unitDao.findByName("pound")
        }
        verify(exactly = 4) { volumeDao.findByAnyName("pound") }
        verify(exactly = 4) { weightDao.findByAnyName("pound") }

        confirmVerified(
            foodController,
            ingredientController,
            recipeController,
            unitDao,
            volumeDao,
            weightDao,
        )
    }

    @Test
    fun `test show Recipe with adjusted servings`() {
        val response = showRecipeController.showRecipe(request, POUND_CAKE_ID, recipe.servings * 2.5)
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

        verify(exactly = 1) {
            foodController.getFood(1L)
            foodController.getFood(2L)
            foodController.getFood(3L)
            foodController.getFood(4L)
        }
        verify(exactly = 1) { ingredientController.getIngredientInventory(POUND_CAKE_ID) }
        verify(exactly = 1) { recipeController.getRecipe(POUND_CAKE_ID) }
        verify(exactly = 4) {
            unitDao.findByAnyNameAndType("pound", UnitType.WEIGHT)
            unitDao.findAllByTypeAndIntlFalse(UnitType.WEIGHT)
            unitDao.findByName("pound")
        }
        verify(exactly = 4) { volumeDao.findByAnyName("pound") }
        verify(exactly = 4) { weightDao.findByAnyName("pound") }

        confirmVerified(
            foodController,
            ingredientController,
            recipeController,
            unitDao,
            volumeDao,
            weightDao,
        )
    }

    companion object {
        const val POUND_CAKE_ID = 75L

        val weightList = listOf(
            Weight("gram", 1.0, true, "g"),
            Weight("pound", 454.0, false, "lb."),
        )

        val unitList = listOf(
            AUnit(1, "milliliter", UnitType.VOLUME, 1.0, true, "ml"),
            AUnit(2, "cup", UnitType.VOLUME, 236.5882365, false, "c."),
            AUnit(3, "gram", UnitType.WEIGHT, 1.0, true, "g"),
            AUnit(4, "pound", UnitType.WEIGHT, 454.0, false, "lb."),
        )

        val foodItemList = listOf(
            FoodItem("sugar", id = 1),
            FoodItem("flour", id = 2),
            FoodItem("eggs", id = 3),
            FoodItem("butter", id = 4),
        )

        val recipe = Recipe("pound cake", "mix", 4, 4L, POUND_CAKE_ID)

        val ingredients = listOf(
            Ingredient(1L, 1.0, "pound", POUND_CAKE_ID, 1),
            Ingredient(2L, 1.0, "pound", POUND_CAKE_ID, 2),
            Ingredient(3L, 1.0, "pound", POUND_CAKE_ID, 3),
            Ingredient(4L, 1.0, "pound", POUND_CAKE_ID, 4),
        )

    }
}