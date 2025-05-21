package xyz.jimh.souschef.parse

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

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
            Executable { assertEquals(8, parsedIngredients.size) },
            Executable { assertEquals("1 lb. can Bumblebee Alaska Salmon", parsedIngredients[0]) },
            Executable { assertEquals("1 pkg. Kraft Philadelphia cream cheese", parsedIngredients[1]) },
            Executable { assertEquals("1 T. fresh lemon juice", parsedIngredients[2]) },
            Executable { assertEquals("2 tsp. grated white or yellow onion", parsedIngredients[3]) },
            Executable { assertEquals("1 tsp. cream medium heat horseradish", parsedIngredients[4]) },
            Executable { assertEquals("¼ tsp. salt", parsedIngredients[5]) },
            Executable { assertEquals("3 T. fresh parsley minced opt.", parsedIngredients[6]) },
            Executable { assertEquals("¼ tsp. liquid smoke", parsedIngredients[7]) },
        )
    }

    @Test
    fun `read empty ingredients`() {
        val emptyIngredients = ""
        val splitter = IngredientSplitter()
        splitter.read(emptyIngredients)

        val parsedIngredients = splitter.getIngredients()
        assertAll(
            Executable { assertEquals(1, parsedIngredients.size) },
            Executable { assertEquals("", parsedIngredients[0]) },
        )
    }
}