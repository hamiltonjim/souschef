package xyz.jimh.souschef.parse

import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class IngredientSplitterTest {

    val ingredients = """
        1 lb. can Bumblebee Alaska Salmon 1 tsp. cream medium heat horseradish
        1 pkg. Kraft Philadelphia cream cheese ¼ tsp. salt ¼ tsp. liquid smoke
        1 T. fresh lemon juice 3 T. fresh parsley minced opt.
        2 tsp. grated white or yellow onion
    """.trimIndent()

        @Test
    fun read() {
        val splitter = IngredientSplitter()
        val lines = ingredients.split("\n")

        for (line in lines) {
            splitter.read(line)
        }

        val parsedIngredients = splitter.getIngredients()

        assertAll(
            { assertEquals(8, parsedIngredients.size) },
            { assertEquals("1 lb. can Bumblebee Alaska Salmon", parsedIngredients[0]) },
            { assertEquals("1 pkg. Kraft Philadelphia cream cheese", parsedIngredients[1]) },
            { assertEquals("1 T. fresh lemon juice", parsedIngredients[2]) },
            { assertEquals("2 tsp. grated white or yellow onion", parsedIngredients[3]) },
            { assertEquals("1 tsp. cream medium heat horseradish", parsedIngredients[4]) },
            { assertEquals("¼ tsp. salt", parsedIngredients[5]) },
            { assertEquals("3 T. fresh parsley minced opt.", parsedIngredients[6]) },
            { assertEquals("¼ tsp. liquid smoke", parsedIngredients[7]) },
        )
    }

    @Test
    fun `read empty ingredients`() {
        val emptyIngredients = ""
        val splitter = IngredientSplitter()
        splitter.read(emptyIngredients)

        val parsedIngredients = splitter.getIngredients()
        assertAll(
            { assertEquals(1, parsedIngredients.size) },
            { assertEquals("", parsedIngredients[0]) },
        )
    }
}