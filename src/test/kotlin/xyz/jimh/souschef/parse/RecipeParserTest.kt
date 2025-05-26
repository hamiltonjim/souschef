package xyz.jimh.souschef.parse

import io.mockk.every
import io.mockk.mockk
import java.io.BufferedReader
import java.io.IOException
import java.io.StringReader
import java.util.Optional
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import xyz.jimh.souschef.ControllerTestBase
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.config.SpringContext
import xyz.jimh.souschef.config.UnitPreference
import xyz.jimh.souschef.config.resetLateInitField
import xyz.jimh.souschef.control.UnitController
import xyz.jimh.souschef.control.UnitControllerTest
import xyz.jimh.souschef.data.Category
import xyz.jimh.souschef.data.CategoryDao
import xyz.jimh.souschef.data.Preference
import xyz.jimh.souschef.data.UnitDao
import xyz.jimh.souschef.data.Volume
import xyz.jimh.souschef.data.Weight
import xyz.jimh.souschef.display.IngredientBuilder
import xyz.jimh.souschef.display.IngredientFormatter
import xyz.jimh.souschef.display.ResourceText

class RecipeParserTest : ControllerTestBase() {

    private lateinit var ingredientFormatter: IngredientFormatter
    private lateinit var unitDao: UnitDao
    private lateinit var unitController: UnitController
    private lateinit var controller: RecipeParser
    private lateinit var categoryDao: CategoryDao

    @BeforeEach
    fun setUp() {
        setupContext()
        unitDao = mockk()
        unitController = mockk()
        ingredientFormatter = IngredientFormatter(unitDao)
        categoryDao = mockk()

        resetLateInitField(IngredientBuilder, "ingredientFormatter")
        resetLateInitField(IngredientBuilder, "unitController")

        every { SpringContext.getBean(IngredientFormatter::class.java) } returns ingredientFormatter
        every { SpringContext.getBean(UnitDao::class.java) } returns unitDao
        every { SpringContext.getBean(UnitController::class.java) } returns unitController
        every { SpringContext.getBean(CategoryDao::class.java) } returns categoryDao

        every { categoryDao.findAllByIdNotNullOrderByName() } returns categoryList
        every { unitDao.findAll() } returns UnitControllerTest.unitList

        every { preferenceDao.findByHostAndKey(any(), "units") } returns
                Optional.of(Preference("localhost", "units", UnitPreference.ANY.name))

        every { unitController.getVolumesAscending() } returns volumeList
        every { unitController.getWeightsAscending() } returns weightList

        controller = RecipeParser(ingredientFormatter)

        Preferences.locale = "en_US"
        Preferences.loadLanguageStrings(force = true)
    }

    @AfterEach
    fun tearDown() {
        teardownContext()
    }

    @Test
    fun buildParserScreen() {
        val response = controller.buildParserScreen(request)
        val html = response.body.toString()

        assertAll(
            { assertTrue(html.contains("<h1 class=\"centered\">Recipe Reader</h1>")) },
            { assertTrue(html.contains("<th>Paste the recipe into the box below.</th>")) },
            { assertTrue(html.contains("<th>Paste the recipe into the box below.</th>")) },
            { assertTrue(html.contains("<textarea rows=\"10\" cols=\"80\" " +
                    "id=\"to-parse\" onkeyup=\"checkLoadFromScreenEnabled(this)\"></textarea>")) },
            { assertTrue(html.contains("<input type=\"button\" id=\"load-from-screen\" " +
                    "value=\"Read Recipe\" onclick=\"loadRecipeFromScreen()\" disabled=\"true\"></input>")) },
            { assertTrue(html.contains("<label for=\"chooser\">Select a file:</label>")) },
            { assertTrue(html.contains("<input id=\"chooser\" type=\"file\" name=\"chooser\" " +
                    "value=\"Choose\" onchange=\"loadRecipeFile(this)\"/>")) },
        )
    }

    @Test
    fun `recipe from empty screen`() {
        assertThrows<IOException> { controller.recipeFromScreen(request,"") }

    }

    @Test
    fun recipeFromScreen() {
        val response = controller.recipeFromScreen(request, "foo")
        val html = response.body.toString()

        assertAll(
            { assertTrue(html.contains("<h1 class=\"centered\">Recipe Reader</h1>")) },
            { assertTrue(html.contains("<input type=\"text\" id=\"recipe-title\" value=\"foo\" " +
                    "class=\"title\"/>")) },
            { assertTrue(html.contains("<option value='' selected='true'></option>")) },
            { assertTrue(html.contains("<textarea rows=\"10\" cols=\"100\" id=\"directions\">")) },
        )
    }

    @Test
    fun `recipeFromScreen handles no servings value`() {
        val response = controller.recipeFromScreen(request, "foo\nServes: x")
        val html = response.body.toString()


        assertAll(
            { assertTrue(html.contains("<input type=\"number\" min=\"0\" id=\"serves\" value=\"0\"/>")) },
        )
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun recipeFromPdfFile() {
        val encodedFileContents = ResourceText.getBase64("static/Carrot Cake.pdf")

        val response = controller.recipeFromFile(request, "application/pdf", encodedFileContents)
        val html = response.body.toString()

        assertAll(
            { assertTrue(html.contains("<h1 class=\"centered\">Recipe Reader</h1>")) },
            { assertTrue(html.contains("<input type=\"text\" id=\"recipe-title\" " +
                    "value=\"Peach Tree Carrot Cake\" class=\"title\"/>")) },
            { assertTrue(html.contains("<option value='' selected='true'></option>")) },
            { assertTrue(html.contains("<textarea rows=\"10\" cols=\"100\" id=\"directions\">")) },
            { assertTrue(html.contains(direction)) },
            { assertTrue(html.contains("grated carrots")) },
            { assertTrue(html.contains("baking soda")) },
            { assertTrue(html.contains("vanilla")) },
            { assertTrue(html.contains("eggs")) },
            { assertTrue(html.contains("sugar")) },
            )
    }

    @Test
    fun recipeFromTextFile() {
        val fileContents = ResourceText.get("static/carrot_cake.txt")

        val response = controller.recipeFromFile(request, "text/plain", fileContents)
        val html = response.body.toString()

        assertAll(
            { assertTrue(html.contains("<h1 class=\"centered\">Recipe Reader</h1>")) },
            { assertTrue(html.contains("<input type=\"text\" id=\"recipe-title\" " +
                    "value=\"Peach Tree Carrot Cake\" class=\"title\"/>")) },
            { assertTrue(html.contains("<option value='' selected='true'></option>")) },
            { assertTrue(html.contains("<textarea rows=\"10\" cols=\"100\" id=\"directions\">")) },
            { assertTrue(html.contains(direction)) },
            { assertTrue(html.contains("grated carrots")) },
            { assertTrue(html.contains("baking soda")) },
            { assertTrue(html.contains("vanilla")) },
            { assertTrue(html.contains("eggs")) },
            { assertTrue(html.contains("sugar")) },
            )
    }

    @Test
    fun `attempt recipe from javascript file`() {
        val fileContents = ResourceText.get("static/fauxAlert.js")

        val response = controller.recipeFromFile(request, "application/x-javascript", fileContents)
        val html = response.body.toString()

        assertAll(
            { assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.statusCode) },
            { assertTrue(html.contains("${HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()}")) },
            { assertTrue(html.contains("application/x-javascript")) },
        )
    }

    @Test
    fun `find or dont find serves string`() {
        val stringReader = StringReader(startWithServings)
        stringReader.use { reader ->
            val bufferedReader = BufferedReader(reader)
            bufferedReader.use { bReader ->
                assertAll(
                    { assertEquals(3, controller.findServings(bReader, "Serves: ")) },
                    { assertEquals(0, controller.findServings(bReader)) },
                )
            }
        }
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

        val volumeList = listOf(
            Volume("cup", 236.5882365, false, "c."),
            Volume("pint", 473.176473, false, "pt."),
            Volume("quart", 946.352946, false, "qt."),
            Volume("gallon", 3785.411784, false, "gal."),
            Volume("liter", 1000.0, true, "l"),
            Volume("fluid ounce", 29.57352956, false, "fl.oz."),
            Volume("tablespoon", 14.78676478, false, "tbsp."),
            Volume("teaspoon", 4.92892159, false, "tsp."),
            Volume("milliliter", 1.0, true, "ml"),
            Volume("firkin", 34068.706056, false,"fk"),
        )

        val weightList = listOf(
            Weight("ounce", 28.34952312, false, "oz."),
            Weight("pound", 453.59237, false, "lb."),
            Weight("kilogram", 1000.0, true, "kg"),
            Weight("dram", 1.7718452, false ),
            Weight("stone", 6350.29318, false, "st."),
            Weight("gram", 1.0, true, "g"),
            Weight("slug", 14593.90293721, false ),
        )

        val direction = """
            Preheat oven to 350°. Trace a circle around a 9 inch cake pan onto a folded piece of parchment paper. Cut out
            two circles and put in the bottom of 2 9 inch cake pans.
            Combine grated carrots, eggs, sugar and vegetable oil in a large mixing bow. Using an electric mixer, beat
            ingredients until well mixed. Add flour, baking soda, baking powder, cinnamon, and salt and beat well. Add
            vanilla and pecans and mix thoroughly, Pour in prepared cake pans and bake in preheated oven for 45 to 50
            minutes until a toothpick comes out clean. Cool for 10 minutes. Invert onto racks, remove parchment paper
            and cool completely.
            Related: Currant Frosting
            Note: This recipe is from The Peach Tree Tea Room. If you travel to Austin, or San Antonio, plan a day trip
            to Fredericksburg an hour and half drive from either city settled by German immigrants. There are numerous
            interesting shops. Make a lunch reservation for The Peach Tree Tea Room and make time to visit Nimitz
            Museum, an interesting review of World War II Naval action.
        """.trimIndent()

        val startWithServings = """
            title
            serves: 3
            ingredients
        """.trimIndent()
    }
}