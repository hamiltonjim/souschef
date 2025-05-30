package xyz.jimh.souschef.data

import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class FoodItemTest {
    val foodItem = FoodItem("sugar")

    @BeforeEach
    fun setup() {
        foodItem.name = "sugar"
    }

    @Test
    fun getName() {
        assertEquals("sugar", foodItem.name)
        assertEquals("sugar", foodItem.component1())
    }

    @Test
    fun setName() {
        assertEquals("sugar", foodItem.name)
        foodItem.name = "powdered sugar"
        assertEquals("powdered sugar", foodItem.name)
    }

    @Test
    fun setDescription() {
        foodItem.description = "foo bar"
        assertEquals("foo bar", foodItem.description)
    }

    @Test
    fun setNotes() {
        foodItem.notes = "foo bar"
        assertEquals("foo bar", foodItem.notes)
    }

    @Test
    fun setId() {
        foodItem.id = 1
        assertEquals(1, foodItem.id)
    }

    @Test
    fun copyTest() {
        val item = foodItem.copy(description = "foo bar")
        assertAll(
            { assertNotEquals(foodItem.description, item.description) },
            { assertEquals(foodItem.name, item.name) },
            { assertEquals(foodItem.notes, item.notes) },
            { assertEquals(foodItem.id, item.id) },
        )
    }
}