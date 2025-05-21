package xyz.jimh.souschef.control

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.Locale
import java.util.Optional
import kotlin.test.DefaultAsserter.fail
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.mockito.ArgumentMatchers.anyLong
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import xyz.jimh.souschef.ControllerTestBase
import xyz.jimh.souschef.config.Broadcaster
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.config.SpringContext
import xyz.jimh.souschef.config.UnitPreference
import xyz.jimh.souschef.config.UnitType
import xyz.jimh.souschef.config.resetLateInitField
import xyz.jimh.souschef.data.Category
import xyz.jimh.souschef.data.CategoryDao
import xyz.jimh.souschef.data.FoodItem
import xyz.jimh.souschef.data.FoodItemDao
import xyz.jimh.souschef.data.Ingredient
import xyz.jimh.souschef.data.IngredientDao
import xyz.jimh.souschef.data.IngredientToSave
import xyz.jimh.souschef.data.PreferenceDao
import xyz.jimh.souschef.data.Recipe
import xyz.jimh.souschef.data.RecipeDao
import xyz.jimh.souschef.data.RecipeToSave
import xyz.jimh.souschef.data.UnitDao
import xyz.jimh.souschef.data.Volume
import xyz.jimh.souschef.data.Weight
import xyz.jimh.souschef.display.HtmlElements
import xyz.jimh.souschef.display.IngredientBuilder
import xyz.jimh.souschef.display.IngredientFormatter

@MockKExtension.CheckUnnecessaryStub
class EditRecipeControllerTest : ControllerTestBase() {
    private lateinit var categoryDao: CategoryDao
    private lateinit var recipeDao: RecipeDao
    private lateinit var foodItemDao: FoodItemDao
    private lateinit var ingredientDao: IngredientDao
    private lateinit var unitDao: UnitDao
    private lateinit var unitController: UnitController
    private lateinit var recipeController: RecipeController
    private lateinit var categoryController: CategoryController

    private lateinit var editRecipeController: EditRecipeController

    private lateinit var ingredientFormatter: IngredientFormatter

    @BeforeEach
    fun init() {
        setupContext()
        categoryDao = mockk()
        recipeDao = mockk()
        foodItemDao = mockk()
        ingredientDao = mockk()
        unitDao = mockk()
        unitController = mockk()
        ingredientFormatter = IngredientFormatter(unitDao)
        preferenceDao = mockk(relaxed = true)
        Preferences.preferenceDao = preferenceDao
        recipeController = mockk()
        categoryController = mockk()
        editRecipeController = EditRecipeController(categoryDao, recipeDao, foodItemDao, ingredientDao)

        // IngredientBuilder has some lateinit fields that will need help
        resetLateInitField(IngredientBuilder, "categoryDao")
        resetLateInitField(IngredientBuilder, "unitController")
        resetLateInitField(IngredientBuilder, "ingredientFormatter")
        resetLateInitField(HtmlElements, "recipeController")
        resetLateInitField(HtmlElements, "categoryController")

        every { SpringContext.getBean(CategoryDao::class.java) } returns categoryDao
        every { SpringContext.getBean(RecipeDao::class.java) } returns recipeDao
        every { SpringContext.getBean(FoodItemDao::class.java) } returns foodItemDao
        every { SpringContext.getBean(IngredientDao::class.java) } returns ingredientDao
        every { SpringContext.getBean(IngredientFormatter::class.java) } returns ingredientFormatter
        every { SpringContext.getBean(UnitDao::class.java) } returns unitDao
        every { SpringContext.getBean(UnitController::class.java) } returns unitController
        every { SpringContext.getBean(PreferenceDao::class.java) } returns preferenceDao
        every { SpringContext.getBean(RecipeController::class.java) } returns recipeController
        every { SpringContext.getBean(CategoryController::class.java) } returns categoryController

        every { categoryDao.findAllByIdNotNullOrderByName() } returns categoryList.toMutableList()
        val slot = slot<Long>()
        every { categoryDao.findById(capture(slot)) } answers {
            Optional.ofNullable(categoryList.firstOrNull { it.id == slot.captured })
        }

        every { preferenceDao.findByHostAndKey(any(), any()) } returns Optional.empty()

        every { unitController.getVolumesAscending() } returns volumeList.toMutableList()
        every { unitController.getWeightsAscending() } returns weightList.toMutableList()

        every { categoryController.findAll() } returns categoryList
        every { recipeController.getRecipes(any()) } returns listOf(recipe)

        // just for coverage
        editRecipeController.listen("foo", "bar")
    }

    @AfterEach
    fun cleanUp() {
        confirmVerified(categoryDao, recipeDao, foodItemDao, ingredientDao, unitDao, preferenceDao, unitController)
    }

    @Test
    fun editRecipe() {
        every { recipeDao.findById(POUND_CAKE_ID) } returns Optional.of(recipe)
        every { ingredientDao.findAllByRecipeId(POUND_CAKE_ID) } returns ingredients.toMutableList()

        val slot = slot<Long>()
        every { foodItemDao.findById(capture(slot)) } answers {
            Optional.ofNullable(foodItemList.first { it.id == slot.captured })
        }

        val newScreenResponse = editRecipeController.editRecipe(request, POUND_CAKE_ID)
        assertNotNull(newScreenResponse.body)
        val body = newScreenResponse.body!!

        val list = mutableListOf<Executable>()
        foodItemList.forEach {
            val exec = Executable {
                assertTrue(body.contains(it.name), "ingredient ${it.name} missing")
            }
            list.add(exec)

        }
        assertAll(list)

        verify {
            categoryDao.findById(allAny())
            categoryDao.findAllByIdNotNullOrderByName()
        }
        verify {
            unitController.getVolumesAscending(allAny())
            unitController.getWeightsAscending(allAny())
        }
        verify { recipeDao.findById(POUND_CAKE_ID) }
        verify { ingredientDao.findAllByRecipeId(POUND_CAKE_ID) }
        verify {
            foodItemDao.findById(1L)
            foodItemDao.findById(2L)
            foodItemDao.findById(3L)
            foodItemDao.findById(4L)
        }
        verify(exactly = 4) { preferenceDao.findByHostAndKey("localhost", "units") }
        verify { preferenceDao.findAllByHost(allAny()) }
    }

    @Test
    fun `edit recipe that does not exist`() {
        every { recipeDao.findById(42L) } returns Optional.empty()

        val response = editRecipeController.editRecipe(request, 42L)
        assertAll(
            Executable { assertEquals(HttpStatus.NOT_FOUND, response.statusCode) },
            Executable { assertEquals(null, response.body) },
        )
        verify(exactly = 1) { recipeDao.findById(42L) }
    }

    @Test
    fun editEvilRecipe() {
        every { recipeDao.findById(POUND_CAKE_ID_EVIL) } returns Optional.of(recipeNoCategory)
        every { ingredientDao.findAllByRecipeId(POUND_CAKE_ID_EVIL) } returns ingredientsEvil.toMutableList()
        every { categoryDao.findById(0L) } returns Optional.empty()
        every { foodItemDao.findById(99L) } returns Optional.empty()

        val slot = slot<Long>()
        every { foodItemDao.findById(capture(slot)) } answers {
            Optional.ofNullable(foodItemList.firstOrNull { it.id == slot.captured })
        }

        val newScreenResponse = editRecipeController.editRecipe(request, POUND_CAKE_ID_EVIL)
        assertNotNull(newScreenResponse.body)
        val body = newScreenResponse.body!!

        val list = mutableListOf<Executable>()
        foodItemList.forEach {
            val exec = Executable {
                assertFalse(body.contains(it.name), "ingredient ${it.name} present when it should not be")
            }
            list.add(exec)
        }
        // find the category selector
        val start = body.indexOf("<label for=\"category\">")
        val end = body.indexOf("<br/>", start)
        val selector = body.substring(start + 1, end)
        list.add(Executable { assertTrue(selector.contains("<option value='' selected")) } )
        list.add(Executable {
            assertTrue(body.contains(
                "<input id='ingred-0' name='ingred-0' type='text' value=''>"),
                "ingredient with missing name is missing")
        })
        assertAll(list)

        verify(exactly = 1) {
            categoryDao.findById(0L)
            categoryDao.findAllByIdNotNullOrderByName()
        }
        verify(exactly = 1) {
            unitController.getVolumesAscending(UnitPreference.ANY)
            unitController.getWeightsAscending(UnitPreference.ANY)
        }
        verify(exactly = 1) { recipeDao.findById(POUND_CAKE_ID_EVIL) }
        verify(exactly = 1) { ingredientDao.findAllByRecipeId(POUND_CAKE_ID_EVIL) }
        verify(exactly = 1) {
            foodItemDao.findById(99L)
        }
        verify(exactly = 1) { preferenceDao.findByHostAndKey("localhost", "units") }
        verify { preferenceDao.findAllByHost(allAny()) }
    }

    @Test
    fun newRecipe() {
        val newScreenResponse = editRecipeController.newRecipe(request, 1L)
        val body = newScreenResponse.body

        assertAll(
            Executable { assertNotNull(body) },
            Executable { assertTrue(body!!.isNotEmpty()) },
            Executable { assertTrue(body!!.contains("<option value='Appetizers' selected='true'>Appetizers</option>")) },
        )

        verify {
            categoryDao.findById(1L)
            categoryDao.findAllByIdNotNullOrderByName()
        }
        verify {
            unitController.getVolumesAscending(allAny())
            unitController.getWeightsAscending(allAny())
        }
        verify(exactly = 1) { preferenceDao.findByHostAndKey("localhost", "units") }
        verify { preferenceDao.findAllByHost(allAny()) }
    }

    @Test
    fun saveRecipe() {
        val stringSlot = slot<String>()
        every { foodItemDao.findByName(capture(stringSlot)) } answers {
            Optional.ofNullable(foodItemList.firstOrNull { it.name == stringSlot.captured })
        }
        val foodItemSlot = slot<FoodItem>()
        every { foodItemDao.save(capture(foodItemSlot)) } answers {
            val foodItem = foodItemSlot.captured
            foodItem.id = 314159
            foodItem
        }
        every { categoryDao.findByName(capture(stringSlot)) } answers {
            Optional.ofNullable(categoryList.firstOrNull { it.name == stringSlot.captured })
        }
        val categorySlot = slot<Category>()
        every { categoryDao.save(capture(categorySlot)) } answers {
            val category = categorySlot.captured
            category.id = 271818
            category
        }

        val recipeSlot = slot<Recipe>()
        every { recipeDao.save(capture(recipeSlot)) } answers {
            if (recipeSlot.captured.id == null) recipeSlot.captured.id = 7L  // magic number (doesn't matter)
            recipeSlot.captured
        }
        every { ingredientDao.findByRecipeIdAndItemId(7L, anyLong()) } returns Optional.empty()
        val longSlot = slot<Long>()
        every { ingredientDao.findByRecipeIdAndItemId(POUND_CAKE_ID, capture(longSlot)) } answers {
            Optional.ofNullable(ingredients.firstOrNull { it.itemId == longSlot.captured })
        }
        val ingredientSlot = slot<Ingredient>()
        every { ingredientDao.save(capture(ingredientSlot)) } answers { ingredientSlot.captured }
        every { ingredientDao.findAllByRecipeId(POUND_CAKE_ID) } returns ingredients.toMutableList()
        every { ingredientDao.findByRecipeIdAndItemId(7L, capture(longSlot)) } answers {
            Optional.of(Ingredient(longSlot.captured, 1.0, "pound", 7L))
        }
        justRun { ingredientDao.deleteAll(any()) }

        val ingredientsToSave = listOf(
            IngredientToSave("sugar", 1.0, "pound", "WEIGHT"),
            IngredientToSave("flour", 1.0, "pound", "WEIGHT"),
            IngredientToSave("eggs", 1.0, "pound", "WEIGHT"),
            // replace one ingredient
            // IngredientToSave("butter", 1.0, "pound", "WEIGHT"),
            IngredientToSave("better", 1.0, "pound", "WEIGHT"),
        )
        val recipeToSave = RecipeToSave(
            null,
            "pound cake",
            "Desserts",
            8,
            "mix",
            ingredientsToSave
        )

        editRecipeController.saveRecipe(recipeToSave)

        val newSave = recipeToSave.copy(id = POUND_CAKE_ID, category = "nothing")
        editRecipeController.saveRecipe(newSave)

        verify(exactly = 2) { recipeDao.save(any()) }
        verify(exactly = 2) { categoryDao.findByName(allAny()) }
        verify(exactly = 1) { categoryDao.save(allAny()) }
        verify(exactly = 1) {
            ingredientDao.findByRecipeIdAndItemId(7L, 1L)
            ingredientDao.findByRecipeIdAndItemId(7L, 2L)
            ingredientDao.findByRecipeIdAndItemId(7L, 3L)
            ingredientDao.findByRecipeIdAndItemId(7L, 314159L)
            ingredientDao.findByRecipeIdAndItemId(POUND_CAKE_ID, 1L)
            ingredientDao.findByRecipeIdAndItemId(POUND_CAKE_ID, 2L)
            ingredientDao.findByRecipeIdAndItemId(POUND_CAKE_ID, 3L)
            ingredientDao.findByRecipeIdAndItemId(POUND_CAKE_ID, 314159L)
        }
        verify(exactly = 8) { ingredientDao.save(any()) }
        verify(exactly = 1) {
            ingredientDao.findAllByRecipeId(POUND_CAKE_ID)
            ingredientDao.deleteAll(any())
        }
        verify(exactly = 2) { recipeDao.save(any()) }
        verify(exactly = 2) {
            foodItemDao.findByName("sugar")
            foodItemDao.findByName("flour")
            foodItemDao.findByName("eggs")
            foodItemDao.findByName("better")
            foodItemDao.save(allAny())
        }
    }

    @Test
    fun `save recipe with errors throws`() {
        val recipeWithNoName = RecipeToSave(
            null,
            "",
            "",
            0,
            "",
            emptyList()
        )
        try {
            editRecipeController.saveRecipe(recipeWithNoName)
            fail("Should have thrown a ResponseStatusException")
        } catch (e: ResponseStatusException) {
            val body = e.message
            assertAll(
                Executable { assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, e.statusCode) },
                Executable { assertTrue(body.contains(EditRecipeController.NO_RECIPE_NAME)) },
                Executable { assertTrue(body.contains(EditRecipeController.NO_INGREDIENTS)) },
                Executable { assertTrue(body.contains(EditRecipeController.NO_SERVINGS)) },
                Executable { assertTrue(body.contains("A category must be chosen.")) },
            )
        } catch (e: Throwable) {
            fail("Should not have thrown a ${e.javaClass.simpleName}")
        }

        val recipeWithNamelessIngredients = RecipeToSave(
            null,
            "name",
            "Category",
            1,
            "",
            listOf(IngredientToSave("", 0.0, "", UnitType.NONE.name)),
        )
        try {
            editRecipeController.saveRecipe(recipeWithNamelessIngredients)
            fail("Should have thrown a ResponseStatusException")
        } catch (e: ResponseStatusException) {
            val body = e.message
            assertAll(
                Executable { assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, e.statusCode) },
                Executable { assertFalse(body.contains(EditRecipeController.NO_RECIPE_NAME), "name") },
                Executable { assertTrue(body.contains(EditRecipeController.NO_INGREDIENTS), "ingredients") },
                Executable { assertFalse(body.contains(EditRecipeController.NO_SERVINGS), "servings") },
            )
        } catch (e: Throwable) {
            fail("Should not have thrown a ${e.javaClass.simpleName}")
        }
    }

    @Test
    fun `test getRecipeLink`() {
        every { recipeController.getRecipe(POUND_CAKE_ID) } returns recipe
        val link = editRecipeController.getRecipeLink(POUND_CAKE_ID)
        assertEquals("<a href='/souschef/show-recipe/$POUND_CAKE_ID'>pound cake</a>", link.body)
    }

    @Test
    fun `check that listener listens`() {
        editRecipeController.init()
        Preferences.broadcast("bar", "foo")
        assertEquals("foo" to "bar", editRecipeController.lastMessage)
        Preferences.broadcast("baz")
        assertEquals(Broadcaster.NO_NAME to "baz", editRecipeController.lastMessage)
        editRecipeController.destroy()
    }

    @Test
    fun `get language`() {
        println(Locale.getDefault())
    }

    companion object {
        val categoryList = listOf(
            Category("Appetizers", 1L),
            Category("Breads", 2L),
            Category("Cookies", 3L),
            Category("Desserts", 4L),
            Category("Drinks", 5L),
            Category("Entrées", 6L),
            Category("invalid", null),
        )

        val volumeList = listOf(
            Volume("milliliter", 1.0, true, "ml"),
            Volume("cup", 236.5882365, false, "c."),
        )

        val weightList = listOf(
            Weight("gram", 1.0, true, "g"),
            Weight("pound", 454.0, false, "lb."),
        )

        val foodItemList = listOf(
            FoodItem("sugar", id = 1),
            FoodItem("flour", id = 2),
            FoodItem("eggs", id = 3),
            FoodItem("butter", id = 4),
        )

        const val POUND_CAKE_ID = 57L
        const val POUND_CAKE_ID_EVIL = 58L
        val recipe = Recipe("pound cake", "mix", 4, 4L, POUND_CAKE_ID)
        val recipeNoCategory = Recipe("pound cake", "mix", 4, 0L, POUND_CAKE_ID_EVIL)

        val ingredients = listOf(
            Ingredient(1L, 1.0, "pound", POUND_CAKE_ID, 1),
            Ingredient(2L, 1.0, "pound", POUND_CAKE_ID, 2),
            Ingredient(3L, 1.0, "pound", POUND_CAKE_ID, 3),
            Ingredient(4L, 1.0, "pound", POUND_CAKE_ID, 4),
        )
        val ingredientsEvil = listOf(
            Ingredient(99L, 1.0, "pound", POUND_CAKE_ID_EVIL, 1),
        )
    }

}