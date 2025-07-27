package xyz.jimh.souschef.control

import jakarta.annotation.PostConstruct
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import xyz.jimh.souschef.data.Ingredient
import xyz.jimh.souschef.data.IngredientDao

@SpringBootTest
class IngredientControllerTest() : ApplicationContextAware {

    private lateinit var context: ApplicationContext
    private lateinit var ingredientDao: IngredientDao
    private lateinit var controller: IngredientController

    @PostConstruct
    fun init() {
        ingredientDao = context.getBean(IngredientDao::class.java)
        ingredientDao.saveAll(ingredients)
    }

    @BeforeEach
    fun setUp() {
        controller = IngredientController(ingredientDao)
    }

    @Test
    fun getIngredients() {
        val list = controller.getIngredients()
        assertTrue { list.containsAll(ingredients) }
    }

    @Test
    fun getIngredient() {
        assertAll(
            { assertEquals(ingredients[0], controller.getIngredient(1L)) },
            { assertEquals(ingredients[1], controller.getIngredient(2L)) },
            { assertEquals(ingredients[2], controller.getIngredient(3L)) },
            { assertThrows<IllegalStateException> { controller.getIngredient(99L) } },
        )
    }

    @Test
    fun getIngredientInventory() {
        val aList = controller.getIngredientInventory(RECIPE_A_ID)
        val bList = controller.getIngredientInventory(RECIPE_B_ID)

        assertAll(
            { assertEquals(3, aList.size) },
            { assertTrue { bList.size >= 4 } },
        )
    }

    @Test
    fun addIngredient() {
        val newIngredient = Ingredient(8L, 0.75,"pound", RECIPE_B_ID, sortIndex = 100)
        val newList = mutableListOf<Ingredient>()
        newList.addAll(ingredients)

        val savedIngredient = controller.addIngredient(newIngredient)
        val bList = controller.getIngredientInventory(RECIPE_B_ID)
        val bIngredient = controller.getIngredient(savedIngredient.id!!)
        assertAll(
            { assertEquals(5, bList.size) },
            { assertTrue(bIngredient in bList) }
        )

        val xIngredient = controller.getIngredientByRecipeAndIndex(RECIPE_B_ID, 10)
        val cIngredient = controller.getIngredientByRecipeAndIndex(RECIPE_B_ID, 100)
        assertAll(
            { assertFalse { xIngredient.isPresent } },
            { assertTrue { cIngredient.isPresent } },
            { assertEquals(bIngredient, cIngredient.get()) },
        )
    }

    @Test
    fun updateIngredient() {
        val list = mutableListOf<Ingredient>()
        list.addAll(ingredients)

        val anyIngredient = ingredients[2]
        anyIngredient.amount = 2.45
        controller.updateIngredient(anyIngredient)
        assertEquals(
            anyIngredient.amount,
            controller.getIngredient(anyIngredient.id!!).amount,
            1e-10)
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }

    companion object {
        const val RECIPE_A_ID = 1L
        const val RECIPE_B_ID = 3L

        val ingredients = listOf(
            Ingredient(1L, 1.0, "cup", RECIPE_B_ID, sortIndex = 2),
            Ingredient(1L, 1.0, "cup", RECIPE_A_ID, sortIndex = 3),
            Ingredient(2L, 1.0, "cup", RECIPE_B_ID, sortIndex = 4),
            Ingredient(3L, 1.0, "cup", RECIPE_A_ID, sortIndex = 5),
            Ingredient(3L, 1.0, "cup", RECIPE_B_ID, sortIndex = 6),
            Ingredient(4L, 1.0, "cup", RECIPE_A_ID, sortIndex = 7),
            Ingredient(4L, 1.0, "cup", RECIPE_B_ID, sortIndex = 8),
        )
    }
}