package xyz.jimh.souschef.display

import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import xyz.jimh.souschef.ControllerTestBase
import xyz.jimh.souschef.config.SpringContext
import xyz.jimh.souschef.config.resetLateInitField
import xyz.jimh.souschef.control.CategoryController
import xyz.jimh.souschef.control.EditRecipeControllerTest.Companion.POUND_CAKE_ID
import xyz.jimh.souschef.control.EditRecipeControllerTest.Companion.recipe
import xyz.jimh.souschef.control.RecipeController

class HtmlElementsTest : ControllerTestBase() {

    private lateinit var recipeController: RecipeController
    private lateinit var categoryController: CategoryController

    @BeforeEach
    fun init() {
        setupContext()
        resetLateInitField(HtmlElements, "recipeController")
        recipeController = mockk()
        categoryController = mockk()

        every { SpringContext.getBean(RecipeController::class.java) } returns recipeController
        every { SpringContext.getBean(CategoryController::class.java) } returns categoryController
    }

    @AfterEach
    fun tearDown() {
        teardownContext()
    }

    @Test
    fun `addRecipeLink test succeeds`() {
        every { recipeController.getRecipe(POUND_CAKE_ID) } returns recipe

        val link = HtmlElements.addRecipeLink(POUND_CAKE_ID)
        assertEquals("<a href='/souschef/show-recipe/$POUND_CAKE_ID'>pound cake</a>", link)
    }

    @Test
    fun `addRecipeLink test fails`() {
        every { recipeController.getRecipe(POUND_CAKE_ID) } throws IllegalStateException()

        assertThrows<IllegalStateException> { HtmlElements.addRecipeLink(POUND_CAKE_ID) }

        // call it again, to cover a branch case
        assertThrows<IllegalStateException> { HtmlElements.addRecipeLink(POUND_CAKE_ID) }
    }
}