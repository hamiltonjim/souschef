package xyz.jimh.souschef.control

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.Optional
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import xyz.jimh.souschef.data.Ingredient
import xyz.jimh.souschef.data.IngredientDao

class IngredientControllerTest {

    private lateinit var ingredientDao: IngredientDao
    private lateinit var controller: IngredientController

    @BeforeEach
    fun setUp() {
        ingredientDao = mockk()
        controller = IngredientController(ingredientDao)
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(ingredientDao)
    }

    @Test
    fun getIngredients() {
        every { ingredientDao.findAll() } returns ingredients

        val list = controller.getIngredients()
        assertEquals(ingredients, list)

        verify { ingredientDao.findAll() }
    }

    @Test
    fun getIngredient() {
        val longSlot = slot<Long>()
        every { ingredientDao.findById(capture(longSlot)) } answers {
            Optional.ofNullable(ingredients.firstOrNull { it.id == longSlot.captured })
        }

        assertAll(
            { assertEquals(ingredients[0], controller.getIngredient(1L)) },
            { assertEquals(ingredients[1], controller.getIngredient(2L)) },
            { assertEquals(ingredients[2], controller.getIngredient(3L)) },
            { assertThrows<IllegalStateException> { controller.getIngredient(99L) } }
        )

        verify(exactly = 4) { ingredientDao.findById(allAny()) }
    }

    @Test
    fun getIngredientInventory() {
        val longSlot = slot<Long>()
        every { ingredientDao.findAllByRecipeId(capture(longSlot)) } answers {
            ingredients.filter { it.recipeId == longSlot.captured }.toMutableList()
        }

        val aList = controller.getIngredientInventory(RECIPE_A_ID)
        val bList = controller.getIngredientInventory(RECIPE_B_ID)

        assertAll(
            { assertEquals(3, aList.size) },
            { assertEquals(4, bList.size) },
        )

        verify { ingredientDao.findAllByRecipeId(allAny()) }
    }

    @Test
    fun addIngredient() {
        val newIngredient = Ingredient(8L, 0.75,"pound", RECIPE_B_ID)
        val newList = mutableListOf<Ingredient>()
        newList.addAll(ingredients)

        val ingredientSlot = slot<Ingredient>()
        every { ingredientDao.save(capture(ingredientSlot)) } answers {
            val ingredient = ingredientSlot.captured
            ingredient.id = ++iCounter
            newList.add(ingredient)
            ingredient
        }

        val longSlot = slot<Long>()
        every { ingredientDao.findAllByRecipeId(capture(longSlot)) } answers {
            newList.filter { it.recipeId == longSlot.captured }.toMutableList()
        }
        every { ingredientDao.findById(capture(longSlot)) } answers {
            Optional.ofNullable(newList.firstOrNull { it.id == longSlot.captured })
        }

        controller.addIngredient(newIngredient)
        val bList = controller.getIngredientInventory(RECIPE_B_ID)
        val bIngredient = controller.getIngredient(iCounter)
        assertAll(
            { assertEquals(5, bList.size) },
            { assertTrue(bIngredient in bList) }
        )

        verify { ingredientDao.findById(allAny()) }
        verify { ingredientDao.save(newIngredient) }
        verify { ingredientDao.findAllByRecipeId(RECIPE_B_ID) }
    }

    @Test
    fun updateIngredient() {
        val list = mutableListOf<Ingredient>()
        list.addAll(ingredients)

        val longSlot = slot<Long>()
        every { ingredientDao.findById(capture(longSlot)) } answers {
            Optional.ofNullable(list.firstOrNull { it.id == longSlot.captured })
        }

        val ingredientSlot = slot<Ingredient>()
        every { ingredientDao.save(capture(ingredientSlot)) } answers { ingredientSlot.captured }

        val anyIngredient = ingredients[2]
        anyIngredient.amount = 2.45
        controller.updateIngredient(anyIngredient)
        assertEquals(
            anyIngredient.amount,
            controller.getIngredient(anyIngredient.id!!).amount,
            1e-10)

        verify {
            ingredientDao.findById(allAny())
            ingredientDao.save(allAny())
        }
    }

    companion object {
        const val RECIPE_A_ID = 1L
        const val RECIPE_B_ID = 3L
        var iCounter: Long = 0

        val ingredients = listOf(
            Ingredient(1L, 1.0, "cup", RECIPE_B_ID),
            Ingredient(1L, 1.0, "cup", RECIPE_A_ID),
            Ingredient(2L, 1.0, "cup", RECIPE_B_ID),
            Ingredient(3L, 1.0, "cup", RECIPE_A_ID),
            Ingredient(3L, 1.0, "cup", RECIPE_B_ID),
            Ingredient(4L, 1.0, "cup", RECIPE_A_ID),
            Ingredient(4L, 1.0, "cup", RECIPE_B_ID),
        )
        @BeforeAll @JvmStatic
        fun initAll() {
            var counter = 0L
            ingredients.forEach {
                it.id = ++counter
            }
            iCounter = counter
        }
    }
}