/*
 * Copyright (c) 2025 Jim Hamilton. All rights reserved.
 */

package xyz.jimh.souschef.control

import io.mockk.every
import io.mockk.mockk
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.config.Preferences.preferenceDao
import xyz.jimh.souschef.config.SpringContext
import xyz.jimh.souschef.data.Category
import xyz.jimh.souschef.data.CategoryDao
import xyz.jimh.souschef.data.Preference
import xyz.jimh.souschef.data.Recipe
import xyz.jimh.souschef.data.RecipeDao

@SpringBootTest
class SearchControllerTest : ApplicationContextAware {
    private lateinit var appContext: ApplicationContext
    private lateinit var searchController: SearchController
    private lateinit var recipeDao: RecipeDao
    private lateinit var recipeController: RecipeController

    @PostConstruct
    fun setUp() {
        recipeDao = appContext.getBean(RecipeDao::class.java)
        inserts()
    }

    @BeforeEach
    fun before() {
        preferenceDao = mockk(relaxed = true)
        every { preferenceDao.findAllByHost(any()) } returns preferenceList

        recipeController = RecipeController(recipeDao)
        searchController = SearchController(recipeController)
    }

    @Test
    fun `ensure recipes are there`() {
        val recipes = recipeDao.findAll()
        assertEquals(3, recipes.size)
    }

    @Test
    fun `ensure recipes with banana are there`() {
        val carrotRecipes = recipeController.searchRecipeTitles("banana")
        assertEquals(1, carrotRecipes.size)
    }

    @Test
    fun search() {
        val request = mockk<HttpServletRequest>()
        every { request.remoteHost } returns "localhost"

        val response = searchController.search(request)
        val body = response.body
        assertNotNull(body)
        assertAll(
            { assertTrue { body.contains("<h1>${Preferences.getLanguageString("searchTitle")}</h1>") } },
            { assertTrue { body.contains("class=\"centered\"") } },
        )
    }

    @Test
    fun searchTitles() {
        val response = searchController.searchTitles("carrot")
        val body = response.body
        assertNotNull(body)
        assertAll(
            { assertTrue { body.contains("Peach Tree Carrot Cake") } },
            { assertTrue { body.contains("Carrot Cake Frosting") } },
        )
    }

    @Test
    fun searchIngredients() {
        val response = searchController.searchIngredients("carrot")
        val body = response.body
        assertNotNull(body)
        assertAll(
            { assertFalse { body.contains("Peach Tree Carrot Cake") } },
            { assertFalse { body.contains("Carrot Cake Frosting") } },
            { assertTrue { body.contains(Preferences.getLanguageString("noRecipesFound")) } },
        )
    }

    @Test
    fun searchDirections() {
        val response = searchController.searchDirections("overly ripe")
        val body = response.body
        assertNotNull(body)
        assertAll(
            { assertFalse { body.contains("Carrot Cake Frosting") } },
            { assertTrue { body.contains("Banana Bread") } },
            { assertFalse { body.contains("Peach Tree Carrot Cake") } },
        )
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        appContext = applicationContext
        SpringContext.setApplicationContext(appContext)
    }

    fun inserts() {
        val categoryDao = appContext.getBean(CategoryDao::class.java)
        categoryDao.saveAll(categories)

        recipeDao.saveAll(recipes)
    }

    companion object {

        val categories = listOf(
            Category("Bread", ),
            Category("Drinks",),
            Category("Entrées",),
            Category("Desserts",),
            Category("Appetizers",),
            Category("Cookies", ),
            Category("Sides", ),
        )


        val recipes = listOf(
            Recipe("Best Ever Banana Bread", """
                The bananas should be overly ripe. The blacker, the better.            
        </ol><br/>""", 8, 1),
            Recipe("Carrot Cake Frosting", """Allow cream cheese to come to room temperature. Using an <br/><br/>
        Related: <a href="http://localhost:8080/souschef/show-recipe/18">Carrot Cake</a>""", 12, 4),
            Recipe("Peach Tree Carrot Cake", """Preheat oven to 350°. Trace a circle around a 9 inch cake pan onto a folded action.""",
                12, 4),
        )

        val preferenceList = listOf(
            Preference("0:0:0:0:0:0:0:1", "showDeleted", "true"),
            Preference("0:0:0:0:0:0:0:1", "unitNames", "abbreviation"),
            Preference("0:0:0:0:0:0:0:1", "language", "en_US"),
            Preference("0:0:0:0:0:0:0:1", "units", "english"),
        )
    }
}