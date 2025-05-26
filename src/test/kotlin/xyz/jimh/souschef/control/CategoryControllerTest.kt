package xyz.jimh.souschef.control

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import xyz.jimh.souschef.data.Category
import xyz.jimh.souschef.data.CategoryCount
import xyz.jimh.souschef.data.CategoryDao
import xyz.jimh.souschef.data.CountDao
import xyz.jimh.souschef.data.Recipe

class CategoryControllerTest {

    private lateinit var categoryDao: CategoryDao
    private lateinit var countDao: CountDao

    private lateinit var controller: CategoryController

    @BeforeEach
    fun setup() {
        categoryDao = mockk()
        countDao = mockk()

        controller = CategoryController(categoryDao, countDao)
    }

    @Test
    fun create() {
        val category = Category("category")
        val catSlot = slot<Category>()
        every { categoryDao.save(capture(catSlot)) } answers {
            val cat = catSlot.captured
            cat.id = ++categoryID
            cat
        }

        val cat = controller.create(category)
        assertAll(
            { assertEquals(categoryID, cat.id) },
            { assertEquals(category.name, cat.name) }
        )

        verify { categoryDao.save(any()) }
        confirmVerified(categoryDao, countDao)
    }

    @Test
    fun findAll() {
        every { categoryDao.findAll() } returns categoryList
        val retrievedList = controller.findAll()
        assertEquals(categoryList, retrievedList)

        verify { categoryDao.findAll() }
        confirmVerified(categoryDao, countDao)
    }

    @Test
    fun findById() {
        val longSlot = slot<Long>()
        every { categoryDao.findById(capture(longSlot)) } answers {
            Optional.ofNullable(categoryList.firstOrNull { it.id == longSlot.captured })
        }
        val category1 = controller.findById(1L)
        val category2 = controller.findById(2L)
        val category3 = controller.findById(3L)
        val category88Opt = controller.findById(88L)
        val category88 = if (category88Opt.isPresent) category88Opt.get() else null

        assertAll(
            { assertEquals("Appetizers", category1.get().name, "Appetizers not found") },
            { assertEquals("Breads", category2.get().name, "Breads not found") },
            { assertEquals("Cookies", category3.get().name, "Cookies not found") },
            { assertNull(category88, "Spurious category 88 found") },
        )

        verify(exactly = 4) { categoryDao.findById(allAny()) }
        confirmVerified(categoryDao, countDao)
    }

    @Test
    fun countByCategory() {
        val countList = recipeList.filter { !it.deleted }
        val count = countByCategory(countList)
        val incCount = countByCategory(recipeList)
        every { countDao.getCategoryCounts(false) } returns count
        every { countDao.getCategoryCounts(true) } returns incCount

        val list1 = controller.countByCategory(Optional.of(false))
        val list2 = controller.countByCategory(Optional.of(true))
        val list3 = controller.countByCategory(Optional.ofNullable(null))

        assertAll(
            { assertTrue(list1.size < list2.size, "list2 should be bigger") },
            { assertEquals(2, list1.size, "incorrect number of counts") },
            { assertEquals(3, list2.size, "incorrect number of counts (del)") },
            { assertEquals(list1, list3, "null case failed") },
        )

        verify(atLeast = 1) {
            countDao.getCategoryCounts(false)
            countDao.getCategoryCounts(true)
        }
        confirmVerified(categoryDao, countDao)
    }

    private fun countByCategory(list: List<Recipe>): List<CategoryCount> {
        val map = mutableMapOf<Long, Int>()
        list.forEach {
            val previous = map[it.categoryId] ?: 0
            map[it.categoryId] = previous + 1
        }
        val resultList = mutableListOf<CategoryCount>()
        map.forEach { (categoryId, count) ->
            val catName = nameOfCategory(categoryId)
            resultList.add(CategoryCount(catName, count))
        }
        return resultList
    }

    private fun nameOfCategory(categoryId: Long): String {
        val category = categoryList.first { it.id == categoryId }
        return category.name
    }

    companion object {
        val categoryList = listOf(
            Category("Appetizers", 1L),
            Category("Breads", 2L),
            Category("Cookies", 3L),
            Category("Desserts", 4L),
            Category("Drinks", 5L),
            Category("Entrées", 6L),
        )
        var categoryID = categoryList.size.toLong()

        const val POUND_CAKE_ID = 57L
        const val EGG_CREAM_ID = 69L
        val recipeList = listOf(
            Recipe("pound cake", "mix", 4, 4L, POUND_CAKE_ID),
            Recipe("egg cream", "stir", 1, 5L, EGG_CREAM_ID),
            Recipe("fake dessert 1", "", 1, 4L, 125L),
            Recipe("fake dessert 2", "", 1, 4L, 126L),
            Recipe("fake dessert 3", "", 1, 4L, 127L),
            Recipe("deleted entrée", "", 1, 6L, 128L, true),
        )
    }

}
