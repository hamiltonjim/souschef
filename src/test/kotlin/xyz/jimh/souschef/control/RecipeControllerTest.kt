package xyz.jimh.souschef.control

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.Instant
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import xyz.jimh.souschef.data.Recipe
import xyz.jimh.souschef.data.RecipeDao

class RecipeControllerTest {

    private lateinit var controller: RecipeController
    private lateinit var recipeDao: RecipeDao

    @BeforeEach
    fun setUp() {
        recipeDao = mockk()
        controller = RecipeController(recipeDao)
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(recipeDao)
    }

    @Test
    fun addRecipe() {
        val recipeSlot = slot<Recipe>()
        every { recipeDao.save(capture(recipeSlot)) } answers {
            val recipe = recipeSlot.captured
            recipe.id = ++id
            recipe
        }

        val recipe = Recipe(
            "foo bars",
            "make shit up",
            3,
            4L
        )
        val saved = controller.addRecipe(recipe)
        assertAll(
            { assertEquals(recipe.name, saved.name) },
            { assertEquals(recipe.directions, saved.directions) },
            { assertEquals(recipe.servings, saved.servings) },
            { assertEquals(recipe.categoryId, saved.categoryId) },
            { assertEquals(id, saved.id) },
        )

        verify { recipeDao.save(recipe) }
    }

    @Test
    fun getRecipes() {
        every { recipeDao.findAllByDeletedIsFalse() } returns recipeList.filter { !it.deleted }

        val recipes = controller.getRecipes()
        assertAll(
            { assertEquals(10, recipes.size) },
            { assertTrue(recipeList[10] !in recipes) },
        )

        verify(exactly = 0) { recipeDao.findAll() }
        verify { recipeDao.findAllByDeletedIsFalse() }
    }

    @Test
    fun getRecipe() {
        val longSlot = slot<Long>()
        every { recipeDao.findById(capture(longSlot)) } answers {
            Optional.ofNullable(recipeList.firstOrNull { it.id == longSlot.captured })
        }

        val applePie = controller.getRecipe(101L)
        val haggis = controller.getRecipe(108L)
        val kangaroo = controller.getRecipe(111L)

        assertAll(
            { assertEquals(recipeList[0], applePie) },
            { assertEquals(recipeList[7], haggis) },
            { assertEquals(recipeList[10], kangaroo) },
            { assertThrows<IllegalStateException> { controller.getRecipe(150L) } }
        )

        verify(exactly = 4) { recipeDao.findById(any()) }
        verify(exactly = 0) { recipeDao.findByName(any()) }
    }

    @Test
    fun `get recipe by name`() {
        val stringSlot = slot<String>()
        every { recipeDao.findByName(capture(stringSlot)) } answers {
            Optional.ofNullable(recipeList.firstOrNull { it.name == stringSlot.captured })
        }

        val applePie = controller.getRecipe("Apple pie")
        val haggis = controller.getRecipe("Haggis")
        val kangaroo = controller.getRecipe("Kangaroo")

        assertAll(
            { assertEquals(recipeList[0], applePie) },
            { assertEquals(recipeList[7], haggis) },
            { assertEquals(recipeList[10], kangaroo) },
            { assertThrows<IllegalStateException> { controller.getRecipe("nothing") } }
        )

        verify(exactly = 0) { recipeDao.findById(any()) }
        verify(exactly = 4) { recipeDao.findByName(any()) }
    }

    @Test
    fun `get recipes by category`() {
        val longSlot = slot<Long>()
        every { recipeDao.findAllByCategoryIdAndDeletedIsFalse(capture(longSlot)) } answers {
            recipeList.filter { it.categoryId == longSlot.captured && !it.deleted }
        }

        val desserts = controller.getRecipes(1L)
        val ethnicEntrees = controller.getRecipes(3L)
        val deviledAppetizers = controller.getRecipes(2L)
        val falafels = controller.getRecipes(4L)
        val jams = controller.getRecipes(5L)
        val emptyCategory = controller.getRecipes(666L)

        assertAll(
            { assertEquals(4, desserts.size) },
            { assertTrue(recipeList[0] in desserts) },
            { assertTrue(recipeList[1] in desserts) },
            { assertTrue(recipeList[2] in desserts) },
            { assertTrue(recipeList[8] in desserts) },
            { assertEquals(3, ethnicEntrees.size) },
            { assertTrue(recipeList[4] in ethnicEntrees) },
            { assertTrue(recipeList[7] in ethnicEntrees) },
            { assertTrue(recipeList[9] in ethnicEntrees) },
            { assertFalse(recipeList[10] in ethnicEntrees) },    // because it's deleted
            { assertEquals(1, deviledAppetizers.size) },
            { assertTrue(recipeList[3] in deviledAppetizers) },
            { assertEquals(1, falafels.size) },
            { assertTrue(recipeList[5] in falafels) },
            { assertEquals(1, jams.size) },
            { assertTrue(recipeList[6] in jams) },
            { assertEquals(0, emptyCategory.size) }
        )

        verify(exactly = 6) { recipeDao.findAllByCategoryIdAndDeletedIsFalse(allAny()) }
    }

    @Test
    fun updateRecipe() {
        val recipeSlot = slot<Recipe>()
        every { recipeDao.save(capture(recipeSlot)) } answers { recipeSlot.captured }

        val applePie = recipeList[0].copy()
        applePie.id = ++id
        val updated = controller.updateRecipe(applePie)
        assertEquals(applePie, updated)
        verify(exactly = 1) { recipeDao.save(applePie) }
    }

    @Test
    fun `delete recipe by id`() {
        val longSlot = slot<Long>()
        every { recipeDao.findById(capture(longSlot)) } answers {
            Optional.ofNullable(recipeList.firstOrNull { it.id == longSlot.captured }?.copy())
        }
        val recipeSlot = slot<Recipe>()
        every { recipeDao.save(capture(recipeSlot)) } answers { recipeSlot.captured }

        val now = Instant.now()
        val applePie = controller.deleteRecipe(101L)
        assertAll(
            { assertEquals(recipeList[0].id, applePie.id) },
            { assertEquals(recipeList[0].name, applePie.name) },
            { assertTrue(applePie.deleted) },
            { assertNotNull(applePie.deletedOn) },
            { assertTrue(now.isBefore(applePie.deletedOn)) },
            { assertThrows<IllegalStateException> { controller.deleteRecipe(314159) } },
        )

        verify { recipeDao.findById(allAny()) }
        verify { recipeDao.save(allAny()) }
    }

    @Test
    fun `delete recipe by name`() {
        val stringSlot = slot<String>()
        every { recipeDao.findByName(capture(stringSlot)) } answers {
            Optional.ofNullable(recipeList.firstOrNull { it.name == stringSlot.captured }?.copy())
        }
        val recipeSlot = slot<Recipe>()
        every { recipeDao.save(capture(recipeSlot)) } answers { recipeSlot.captured }

        val now = Instant.now()
        val applePie = controller.deleteRecipe("Apple pie")
        assertAll(
            { assertEquals(recipeList[0].id, applePie.id) },
            { assertEquals(recipeList[0].name, applePie.name) },
            { assertTrue(applePie.deleted) },
            { assertNotNull(applePie.deletedOn) },
            { assertTrue(now.isBefore(applePie.deletedOn)) },
            { assertThrows<IllegalStateException> { controller.deleteRecipe("3.14 pi") } },
        )

        verify { recipeDao.findByName(allAny()) }
        verify { recipeDao.save(allAny()) }
    }

    companion object {
        var id: Long = 1000

        val recipeList = listOf(
            Recipe("Apple pie", "bake", 8, 1L, 101L),
            Recipe("Banana pudding", "bake", 8, 1L, 102L),
            Recipe("Carrot cake", "bake", 12, 1L, 103L),
            Recipe("Deviled eggs", "boil", 12, 2L, 104L),
            Recipe("Eggplant parmigiana", "bake", 8, 3L, 105L),
            Recipe("Falafel", "no idea", 2, 4L, 106L),
            Recipe("Guava jam", "no idea", 20, 5L, 107L),
            Recipe("Haggis", "no idea", 4, 3L, 108L),
            Recipe("Ice cream", "freeze", 16, 1L, 109L),
            Recipe("Jambalaya", "boil", 8, 3L, 110L),

            // and one that's deleted
            Recipe("Kangaroo", "broil", 4, 3L, 111L, deleted = true),
        )
    }
}