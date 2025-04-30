package xyz.jimh.souschef.display

import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
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

    @Test
    fun `addRecipeLink test succeeds`() {
        setupContext()
        resetLateInitField(HtmlElements, "recipeController")
        val recipeController = mockk<RecipeController>()
        val categoryController = mockk<CategoryController>()

        every { SpringContext.getBean(RecipeController::class.java) } returns recipeController
        every { SpringContext.getBean(CategoryController::class.java) } returns categoryController

        every { recipeController.getRecipe(POUND_CAKE_ID) } returns recipe

        val link = HtmlElements.addRecipeLink(POUND_CAKE_ID)
        assertEquals("<a href='/souschef/show-recipe/$POUND_CAKE_ID'>pound cake</a>", link)
    }

    @Test
    fun `addRecipeLink test fails`() {
        setupContext()
        resetLateInitField(HtmlElements, "recipeController")
        val recipeController = mockk<RecipeController>()
        val categoryController = mockk<CategoryController>()

        every { SpringContext.getBean(RecipeController::class.java) } returns recipeController
        every { SpringContext.getBean(CategoryController::class.java) } returns categoryController

        every { recipeController.getRecipe(POUND_CAKE_ID) } throws IllegalStateException()

        assertThrows<IllegalStateException> { HtmlElements.addRecipeLink(POUND_CAKE_ID) }

        // call it again, to cover a branch case
        assertThrows<IllegalStateException> { HtmlElements.addRecipeLink(POUND_CAKE_ID) }
    }
}