package xyz.jimh.souschef.info

import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.actuate.info.Info
import xyz.jimh.souschef.data.CategoryCount
import xyz.jimh.souschef.data.CountDao

class RecipeCountTest {
    private lateinit var countDao: CountDao

    @BeforeEach
    fun setup() {
        countDao = mockk()
    }

    @Test
    fun `test contribute`() {
        every { countDao.getCategoryCounts(any()) } returns counts

        val counter = RecipeCount(countDao)
        val builder = Info.Builder()
        counter.contribute(builder)
        println(builder)
        val info = builder.build()
        val details = info.details
        assertAll(
            { assertEquals(1, details.size) },
            { assertEquals(counts, details["recipesByCategory"]) },
        )
    }

    @Test
    fun `null builder`() {
        val counter = RecipeCount(countDao)
        assertThrows<IllegalArgumentException> { counter.contribute(null) }
    }

    companion object {
        val counts = listOf(
            CategoryCount("Appetizers", 2),
            CategoryCount("Breads", 9),
            CategoryCount("Cookies", 16),
            CategoryCount("Desserts", 24),
            CategoryCount("Drinks", 2),
            CategoryCount("Entrées", 8),
        )
    }
}