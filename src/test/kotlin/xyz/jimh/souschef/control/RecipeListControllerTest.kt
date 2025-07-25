package xyz.jimh.souschef.control

import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import java.util.Optional
import kotlin.test.DefaultAsserter.fail
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.function.Executable
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import xyz.jimh.souschef.ControllerTestBase
import xyz.jimh.souschef.config.Broadcaster
import xyz.jimh.souschef.config.Listener
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

    @BeforeEach
    fun setup() {
        setupContext()
        categoryDao = mockk()
        recipeDao = mockk()

        controller = RecipeListController(recipeDao, categoryDao)

        preferenceDao = mockk()
        Preferences.preferenceDao = preferenceDao
        every { preferenceDao.findAllByHost("localhost") } returns prefList
        every { SpringContext.getBean(PreferenceDao::class.java) } returns preferenceDao
        every { categoryDao.findAll() } returns categoryList.toMutableList()

        Preferences.locale = "en_US"
        Preferences.loadLanguageStrings(force = true)
    }

    @Test
    fun getCategoryList() {
        val response = controller.getCategoryList(request)
        doCategoryListTest(response.body)

        verify(exactly = 1) { categoryDao.findAll() }
        verify { preferenceDao.findAllByHost(allAny()) }
    }

    private fun doCategoryListTest(body: String?) {
        assertNotNull(body)

        val executables = mutableListOf<Executable>()
        categoryList.forEach {
            executables.add { assertTrue(body.contains(it.name), "${it.name} is missing") }
        }
        assertAll(executables)
    }

    @Test
    fun `check that listener listens`() {
        controller.init()
        Preferences.broadcast("bar", "foo")
        assertEquals(Listener.Message("foo", "bar", Preferences), controller.lastMessage)
        Preferences.broadcast("baz")
        assertEquals(Listener.Message(Broadcaster.NO_NAME, "baz", Preferences), controller.lastMessage)
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
        assertAll(
            { assertNotNull(response.body) },
            { assertTrue(response.body!!.contains(category), "category created") },
        )

        verify {
            categoryDao.save(any<Category>())
            categoryDao.findAll()
        }
        verify { preferenceDao.findAllByHost(allAny()) }
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
        verify { preferenceDao.findAllByHost(allAny()) }
    }

    @Test
    fun `check last message received`() {
        val oldTime = controller.lastMessage?.time

        controller.listen("foo", "bar", Preferences)
        val newTime = controller.lastMessage?.time
        val message = controller.lastMessage
        assertAll(
            { assertEquals(Listener.Message("foo", "bar", Preferences), message,
                "Last message received from server") },
            { assertTrue(newTime != null && (oldTime == null || newTime > oldTime),
                "Last message received from server") }
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
            Executable { assertNotNull(response.body) },
            Executable { assertFalse(
                response.body!!.contains("Pineapple Upside-down Cake"),
                "deleted recipe is present"
            ) }
        )
        deletedList.forEach {
            val exec = Executable {
                assertTrue(response.body!!.contains(it.name), "${it.name} is missing")
            }
            executables.add(exec)
        }
        assertAll(executables)

        // for coverage of a corner case, delete recipe a second time
        val response2 = controller.deleteRecipe(request, 2, DESSERTS, Optional.of(false))
        assertEquals(response, response2)

        // and another corner case, delete a non-existent recipe (use default value for undelete)
        every { recipeDao.findById(23456L) } returns Optional.empty()
        val response3 = controller.deleteRecipe(request, 23456, DESSERTS, Optional.empty())
        assertEquals(response, response3)

        // now test undelete
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
        verify { preferenceDao.findAllByHost(allAny()) }
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
        val executables = mutableListOf(Executable{ assertNotNull(response.body) })
        recipeList.forEach {
            val exec = if (it.deleted)
                Executable { assertFalse(
                    response.body!!.contains(it.name),
                    "${it.name} is present"
                ) }
            else
                Executable { assertTrue(
                    response.body!!.contains(it.name),
                    "${it.name} is missing"
                ) }
            executables.add(exec)
        }
        assertAll(executables)

        response = controller.getRecipeList(request, DESSERTS)
        executables.clear()
        executables.add(Executable { assertNotNull(response.body) })
        recipeList.forEach {
            val exec = Executable { assertTrue(
                response.body!!.contains(it.name),
                "${it.name} is missing"
            ) }
            executables.add(exec)
        }
        assertAll(executables)

        verify(exactly = 2) {
            categoryDao.findAll()
            categoryDao.findById(DESSERTS)
            preferenceDao.findByHostAndKey("localhost", "showDeleted")
        }
        verify { preferenceDao.findAllByHost(allAny()) }
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
        verify { preferenceDao.findAllByHost(allAny()) }
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(recipeDao, categoryDao, preferenceDao)
    }

    companion object {
        const val DESSERTS = 4L

        val prefList = listOf(
            Preference("localhost", "showDeleted", "false"),
            Preference("localhost", "language", "en_US"),
            Preference("localhost", "units", "english"),
            Preference("localhost", "unitNames", "abbreviation")
        )

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
