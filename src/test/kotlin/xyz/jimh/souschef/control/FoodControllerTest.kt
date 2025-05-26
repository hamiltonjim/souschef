package xyz.jimh.souschef.control

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.function.Executable
import xyz.jimh.souschef.data.FoodItem
import xyz.jimh.souschef.data.FoodItemDao

class FoodControllerTest {

    private lateinit var foodItemDao: FoodItemDao
    private lateinit var controller: FoodController

    @BeforeEach
    fun setUp() {
        foodItemDao = mockk()
        controller = FoodController(foodItemDao)
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(foodItemDao)
    }

    @Test
    fun postFood() {
        val foodSet = mutableListOf<FoodItem>()
        foodSet.addAll(list)

        val foodItemSlot = slot<FoodItem>()
        every { foodItemDao.save(capture(foodItemSlot)) } answers {
            val foodItem = foodItemSlot.captured
            foodItem.id = ++counter
            foodSet.add(foodItem)
            foodItem
        }

        val foodItem = FoodItem("eggs")
        controller.postFood(foodItem)

        assertAll(
            { assertEquals(list.size + 1, foodSet.size) },
            { assertEquals(counter, foodItem.id) }
        )
        verify { foodItemDao.save(allAny()) }
    }

    @Test
    fun getFood() {
        val longSlot = slot<Long>()
        every { foodItemDao.findById(capture(longSlot)) } answers {
            Optional.ofNullable(list.firstOrNull { it.id == longSlot.captured })
        }

        assertAll(
            { assertEquals(list[0], controller.getFood(1L).get()) },
            { assertEquals(list[1], controller.getFood(2L).get()) },
            { assertEquals(list[2], controller.getFood(3L).get()) },
            { assertTrue(controller.getFood(4L).isEmpty) }
        )

        verify(exactly = 4) { foodItemDao.findById(allAny()) }
    }

    @Test
    fun getAllFood() {
        val foodSet = mutableListOf<FoodItem>()
        foodSet.addAll(list)

        val foodItemSlot = slot<FoodItem>()
        every { foodItemDao.save(capture(foodItemSlot)) } answers {
            val foodItem = foodItemSlot.captured
            foodItem.id = ++counter
            foodSet.add(foodItem)
            foodItem
        }

        val foodItem = FoodItem("beef")
        controller.postFood(foodItem)

        every { foodItemDao.findAll() } returns foodSet
        val food = controller.getAllFood()

        val execList = mutableListOf<Executable>()
        foodSet.forEach {
            val exec = {
                assertTrue(it in food, "${it.name} is missing")
            }
            execList.add(exec)
        }
        assertAll(execList)

        verify {
            foodItemDao.findAll()
            foodItemDao.save(allAny())
        }
    }

    companion object {
        var counter = 10L
        val list = listOf(
            FoodItem("sugar", id = 1L),
            FoodItem("salt", id = 2L),
            FoodItem("flour", id = 3L)
        )
    }
}