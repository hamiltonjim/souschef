package xyz.jimh.souschef.control

import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import java.util.Optional
import kotlin.test.DefaultAsserter.fail
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import xyz.jimh.souschef.ControllerTestBase
import xyz.jimh.souschef.config.Broadcaster
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.config.SpringContext
import xyz.jimh.souschef.data.Category
import xyz.jimh.souschef.data.CategoryDao
import xyz.jimh.souschef.data.Preference
import xyz.jimh.souschef.data.PreferenceDao
import xyz.jimh.souschef.data.Recipe
import xyz.jimh.souschef.data.RecipeDao

class RecipeListControllerTest : ControllerTestBase() {

    private lateinit var categoryDao: CategoryDao
    private lateinit var recipeDao: RecipeDao
    private lateinit var controller: RecipeListController

    private lateinit var preferenceDao: PreferenceDao

    @BeforeEach
    fun setup() {
        setupContext()
        categoryDao = mockk()
        recipeDao = mockk()

        controller = RecipeListController(recipeDao, categoryDao)

        preferenceDao = mockk()
        Preferences.preferenceDao = preferenceDao
        every { SpringContext.getBean(PreferenceDao::class.java) } returns preferenceDao
        every { categoryDao.findAll() } returns categoryList.toMutableList()

    }

    @Test
    fun getDefault() {
        val response = controller.getDefault(request)
        doCategoryListTest(response.body)

        verify(exactly = 1) { categoryDao.findAll() }
    }

    @Test
    fun getCategoryList() {
        val response = controller.getCategoryList(request)
        doCategoryListTest(response.body)

        verify(exactly = 1) { categoryDao.findAll() }
    }

    private fun doCategoryListTest(body: String?) {
        Assertions.assertNotNull(body)
        check(body != null) { "Request body can't be null at this point; just casting it to non-nullable" }

        val executables = mutableListOf<Executable>()
        categoryList.forEach {
            executables.add { Assertions.assertTrue(body.contains(it.name), "${it.name} is missing") }
        }
        Assertions.assertAll(executables)
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
    fun addCategory() {
        try {
            controller.addCategory(request, " ")
            fail("Expected an IllegalArgumentException to be thrown for blank argument")
        } catch (_: Exception) {}
        try {
            controller.addCategory(request, "")
            fail("Expected an IllegalArgumentException to be thrown for empty argument")
        } catch (_: Exception) {}

        clearMocks(categoryDao)

        val categorySlot = slot<Category>()
        every { categoryDao.save(capture(categorySlot)) } answers {
            val catName = categorySlot.captured.name
            Category(catName, 3L)
        }

        val category = "category"
        val newCategoryList = categoryList.toMutableList()
        val newCategory = Category(category)
        newCategoryList.add(newCategory)
        every { categoryDao.findAll() } returns newCategoryList

        val response = controller.addCategory(request, category)
        Assertions.assertAll(
            Executable { Assertions.assertNotNull(response.body) },
            Executable { Assertions.assertTrue(response.body!!.contains(category), "category created") },
        )

        verify {
            categoryDao.save(any<Category>())
            categoryDao.findAll()
        }
    }

    @Test
    fun `add duplicate category`() {
        val stringSlot = slot<Category>()
        every { categoryDao.save(capture(stringSlot)) } answers {
            stringSlot.captured
        } andThenThrows
                DataIntegrityViolationException("Category already exists")
        controller.addCategory(request, "foo")
        val conflict = controller.addCategory(request, "foo")
        assertEquals(HttpStatus.CONFLICT, conflict.statusCode)

        verify { categoryDao.save(allAny()) }
        verify { categoryDao.findAll() }
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
    fun deleteRecipe() {
        every { categoryDao.findById(DESSERTS) } returns Optional.of(Category("Desserts", DESSERTS))

        val undeletedList = recipeList.filter { !it.deleted }.toMutableList()
        undeletedList[2] = undeletedList[2].copy()
        undeletedList[2].deleted = true
        val deletedList = undeletedList.filter { !it.deleted }
        every { recipeDao.findAllByCategoryIdAndDeletedIsFalse(DESSERTS) } returns deletedList
        every { recipeDao.findById(2L) } returns Optional.of(recipeList[2].copy())

        val fPref = Optional.of(Preference("localhost", "showDeleted", "false"))
        every { preferenceDao.findByHostAndKey(any(), any()) } returns fPref

        val recipeSlot = slot<Recipe>()
        every { recipeDao.save(capture(recipeSlot)) } answers { recipeSlot.captured }

        val response = controller.deleteRecipe(request, 2, DESSERTS, Optional.of(false))
        val executables = mutableListOf(
            Executable { Assertions.assertNotNull(response.body) },
            Executable { Assertions.assertFalse(
                response.body!!.contains("Pineapple Upside-down Cake"),
                "deleted recipe is present"
            ) }
        )
        deletedList.forEach {
            val exec = Executable {
                Assertions.assertTrue(response.body!!.contains(it.name), "${it.name} is missing")
            }
            executables.add(exec)
        }
        Assertions.assertAll(executables)

        // for coverage of a corner case, delete recipe a second time
        val response2 = controller.deleteRecipe(request, 2, DESSERTS, Optional.of(false))
        assertEquals(response, response2)

        // and another corner case, delete a non-existent recipe (use default value for undelete)
        every { recipeDao.findById(23456L) } returns Optional.empty()
        val response3 = controller.deleteRecipe(request, 23456, DESSERTS, Optional.empty())
        assertEquals(response, response3)

        // now test an undelete
        val response4 = controller.deleteRecipe(request, 2, DESSERTS, Optional.of(true))
        assertEquals(response, response4)

        verify {
            recipeDao.findById(allAny())
            recipeDao.save(any())
            recipeDao.findAllByCategoryIdAndDeletedIsFalse(DESSERTS)
        }
        verify {
            categoryDao.findById(DESSERTS)
            categoryDao.findAll()
        }
        verify { preferenceDao.findByHostAndKey("localhost", "showDeleted") }
    }

    @Test
    fun getRecipeList() {
        every { categoryDao.findById(DESSERTS) } returns Optional.of(Category("Desserts", DESSERTS))

        val fullList = recipeList.toMutableList()
        val undeletedList = fullList.filter { !it.deleted }
        every { recipeDao.findAllByCategoryIdAndDeletedIsFalse(DESSERTS) } returns undeletedList
        every { recipeDao.findAllByCategoryId(DESSERTS) } returns fullList

        val fPref = Optional.of(Preference("localhost", "showDeleted", "false"))
        val tPref = Optional.of(Preference("localhost", "showDeleted", "true"))
        every { preferenceDao.findByHostAndKey("localhost", "showDeleted") } returns fPref andThen tPref

        var response = controller.getRecipeList(request, DESSERTS)
        val executables = mutableListOf(Executable { Assertions.assertNotNull(response.body) })
        recipeList.forEach {
            val exec = if (it.deleted)
                Executable { Assertions.assertFalse(
                    response.body!!.contains(it.name),
                    "${it.name} is present"
                ) }
            else
                Executable { Assertions.assertTrue(
                    response.body!!.contains(it.name),
                    "${it.name} is missing"
                ) }
            executables.add(exec)
        }
        Assertions.assertAll(executables)

        response = controller.getRecipeList(request, DESSERTS)
        executables.clear()
        executables.add(Executable { Assertions.assertNotNull(response.body) })
        recipeList.forEach {
            val exec = Executable { Assertions.assertTrue(
                response.body!!.contains(it.name),
                "${it.name} is missing"
            ) }
            executables.add(exec)
        }
        Assertions.assertAll(executables)

        verify(exactly = 2) {
            categoryDao.findAll()
            categoryDao.findById(DESSERTS)
            preferenceDao.findByHostAndKey("localhost", "showDeleted")
        }
        verifyOrder {
            recipeDao.findAllByCategoryIdAndDeletedIsFalse(DESSERTS)
            recipeDao.findAllByCategoryId(DESSERTS)
        }
    }

    @Test
    fun `get recipe list for missing category`() {
        every { categoryDao.findById(99L) } returns Optional.empty()

        val fullList = emptyList<Recipe>()
        every { recipeDao.findAllByCategoryIdAndDeletedIsFalse(99L) } returns fullList

        val fPref = Optional.of(Preference("localhost", "showDeleted", "false"))
        val tPref = Optional.of(Preference("localhost", "showDeleted", "true"))
        every { preferenceDao.findByHostAndKey("localhost", "showDeleted") } returns fPref andThen tPref

        try {
            controller.getRecipeList(request, 99L)
            fail("Should have thrown an exception for category not found")
        } catch (e: ResponseStatusException) {
            assertEquals(HttpStatus.NOT_FOUND, e.statusCode)
        }
        verify {
            categoryDao.findAll()
            categoryDao.findById(99L)
        }
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(recipeDao, categoryDao, preferenceDao)
        clearAllMocks()
    }

    companion object {
        const val DESSERTS = 4L

        val categoryList = listOf(
            Category("Appetizers", 1L),
            Category("Breads", 2L),
            Category("Cookies", 3L),
            Category("Desserts", DESSERTS),
            Category("Drinks", 5L),
            Category("Entrées", 6L),
        )
        val recipeList = listOf(
            Recipe("Carrot Cake", "", 12, DESSERTS, 1L),
            Recipe("Pound Cake", "", 8, DESSERTS, 2L),
            Recipe("Pineapple Upside-down Cake", "", 10, DESSERTS, 3L),
            Recipe("Broccoli Pie", "", 10, 4L, DESSERTS, true),
        )
    }
}
