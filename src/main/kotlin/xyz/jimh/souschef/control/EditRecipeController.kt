/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.control

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.servlet.http.HttpServletRequest
import java.time.Instant
import java.util.Collections.singletonMap
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import xyz.jimh.souschef.config.Broadcaster
import xyz.jimh.souschef.config.Listener
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.data.Category
import xyz.jimh.souschef.data.CategoryDao
import xyz.jimh.souschef.data.Errors
import xyz.jimh.souschef.data.FoodItem
import xyz.jimh.souschef.data.FoodItemDao
import xyz.jimh.souschef.data.Ingredient
import xyz.jimh.souschef.data.IngredientDao
import xyz.jimh.souschef.data.Preference
import xyz.jimh.souschef.data.Recipe
import xyz.jimh.souschef.data.RecipeDao
import xyz.jimh.souschef.data.RecipeToSave
import xyz.jimh.souschef.display.HtmlElements
import xyz.jimh.souschef.display.HtmlElements.TABLE_NAME
import xyz.jimh.souschef.display.IngredientBuilder
import xyz.jimh.souschef.display.ResourceText

/**
 * Controller that builds the [Recipe] editing screen, and handles requests
 * from that screen.
 *
 * @constructor Automagically built with several data access objects.
 */
@RestController
class EditRecipeController(
    private val categoryDao: CategoryDao,
    private val recipeDao: RecipeDao,
    private val foodItemDao: FoodItemDao,
    private val ingredientDao: IngredientDao,
) : Listener {

    private val kLogger = KotlinLogging.logger {}
    internal var lastMessage: Pair<String, Any>? = null
    internal var lastMessageTime: Instant? = null

    /**
     * On startup, binds this [Listener] to [Preferences] (as [Broadcaster])
     */
    @PostConstruct
    fun init() {
        Preferences.addListener(this)
    }

    /**
     * On shutdown, unbinds from [Preferences].
     */
    @PreDestroy
    fun destroy() {
        Preferences.removeListener(this)
    }

    /**
     * Listener for changes in [Preference] values.
     */
    override fun listen(name: String, value: Any) {
        lastMessage = Pair(name, value)
        lastMessageTime = Instant.now()
        kLogger.debug { "listen: $name=$value" }
    }

    /**
     * Builds the screen for editing the [Recipe] with the given [recipeId].
     */
    @Operation(summary = "Produces an edit screen for the recipe with the given id")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Edit screen",
            content = [Content(mediaType = "text/html; charset=UTF-8")]
        ),
    )
    @GetMapping("/edit-recipe/{recipeId}", produces = [MediaType.TEXT_HTML_VALUE])
    fun editRecipe(
        request: HttpServletRequest,
        @PathVariable("recipeId") recipeId: Long
    ): ResponseEntity<String> {
        val recipeOptional = recipeDao.findById(recipeId)
        if (recipeOptional.isEmpty) {
            return ResponseEntity.notFound().build()
        }
        return doEditRecipe(request, recipeOptional.get())
    }

    /**
     * Builds the screen for creating a new [Recipe].
     */
    @Operation(summary = "Produces an edit screen for a new recipw")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Edit screen",
            content = [Content(mediaType = "text/html; charset=UTF-8")]
        ),
    )
    @GetMapping("/new-recipe/{categoryId}", produces = [MediaType.TEXT_HTML_VALUE])
    fun newRecipe(request: HttpServletRequest, @PathVariable categoryId: Long): ResponseEntity<String> {
        val recipe = Recipe("", "", 0, categoryId)
        return doEditRecipe(request, recipe)
    }

    /**
     * Build the edit screen. Works for either an existing [Recipe] or a new one.
     */
    private fun doEditRecipe(
        request: HttpServletRequest,
        recipe: Recipe
    ): ResponseEntity<String> {
        Preferences.loadPreferenceValues(request)
        val remoteHost = request.remoteHost
        val html = Preferences.initHtml()

        html.addHeaderWhitespace().addHeaderElement("style")
            .addHeaderWhitespace().addHeaderText(ResourceText.getStatic("editor.css"))
            .addHeaderWhitespace().closeHeaderElement()

        val ingredients = when {
            recipe.id == null -> emptyList()
            else -> {
                val recipeId = recipe.id!!
                ingredientDao.findAllByRecipeId(recipeId)
            }
        }

        val recipeId = recipe.categoryId
        val category = categoryDao.findById(recipeId)
        val catName = when {
            category.isPresent -> category.get().name
            else -> ""
        }
        HtmlElements.addCategorySelector(html, catName)

        html.addBodyElement("label", mapOf("class" to "title", "for" to "recipe-title"))
            .addBodyText(Preferences.getLanguageString("title"))
            .closeBodyElement()

        html.addBodyElement(
            "input",
            mapOf(
                "id" to "recipe-title",
                "type" to "text",
                "value" to recipe.name,
                "placeholder" to Preferences.getLanguageString("titlePlaceholder"),
                "class" to "title",
            ),
            true
        )

        // save button
        html.addWhitespace().addBodyElement(
            "input",
            mapOf(
                "class" to "title",
                "value" to Preferences.getLanguageString("Save"),
                "type" to "button",
                "onclick" to "doSave('$TABLE_NAME')"
            ),
            true
        )
            .addBreak().addBreak()

        html.addBodyElement("label", singletonMap("for", "serves"))
            .addBodyText(Preferences.getLanguageString("Serves"))
            .closeBodyElement()

        html.addBodyElement(
            "input", mapOf(
                "type" to "number", "id" to "serves", "value" to when {
                    recipe.servings == 0 -> ""
                    else -> "${recipe.servings}"
                }
            ), true
        ).addBreak().addBreak()

        // hidden elements:
        // ingredient counter
        html.addBodyElement(
            "input",
            mapOf("type" to "hidden", "id" to "ingredient-counter", "value" to "${ingredients.size}"),
            true
        )

        // recipe id
        html.addBodyElement(
            tag = "input",
            attributes = mapOf(
                "type" to "hidden", "id" to "recipe-id", "value" to when {
                    recipe.id == null -> "null"
                    else -> "${recipe.id}"
                }
            ),
            closing = true
        )

        // Ingredient list
        HtmlElements.startEditIngredientsTable(html)

        var counter = 0
        if (ingredients.isEmpty()) {
            // add one empty row
            html.startRow()
                .startCell()
                .addBodyText(IngredientBuilder.buildAmountInput("amount-0", 0.0))
                .closeBodyElement()

                .startCell()
                .addBodyText(IngredientBuilder.buildUnitSelector(remoteHost, "unit-", ""))
                .closeBodyElement()

                .startCell()
                .addBodyText(IngredientBuilder.buildIngredientInput("ingred-", ""))
                .closeBodyElement()

                .startCell()
                .addBodyText(IngredientBuilder.buildDeleteIngredient(TABLE_NAME))
                .closeBodyElement()

                .closeBodyElement() // row
            counter++
        }
        ingredients.forEach {
            val foodOption = foodItemDao.findById(it.itemId)
            val name = when {
                foodOption.isPresent -> foodOption.get().name
                else -> ""
            }
            val unit = it.unit ?: ""
            html.startRow()

                .startCell()
                .addBodyText(IngredientBuilder.buildAmountInput("amount-$counter", it.amount))
                .closeBodyElement()

                .startCell().addBodyText(IngredientBuilder.buildUnitSelector(remoteHost, "unit-$counter", unit))
                .closeBodyElement()

                .startCell().addBodyText(IngredientBuilder.buildIngredientInput("ingred-$counter", name))
                .closeBodyElement()

                .startCell().addBodyText(IngredientBuilder.buildDeleteIngredient(TABLE_NAME))
                .closeBodyElement()

                .closeBodyElement() // row

            ++counter
        }
        html.closeBodyElement().addBreak()

        // "add ingredient" button
        html.addBodyElement(
            "input",
            mapOf("type" to "button", "value" to Preferences.getLanguageString("AddIngredient"), "onclick" to "insertNewRow('$TABLE_NAME')"),
            true
        ).addBreak().addBreak()

        // Table for directions (raw and rendered)
        html.startTable()
            .startRow() // header row
            .startHeadingCell().addBodyText(Preferences.getLanguageString("EditDirections")).closeBodyElement()
            .startHeadingCell().closeBodyElement()
            .startHeadingCell().addBodyText(Preferences.getLanguageString("ViewDirections")).closeBodyElement()
            .closeBodyElement() // header row
            .startRow() // directions
            .startCell(mapOf("class" to "render"))    // raw

        html
            .addBodyElement(
                "textArea",
                mapOf("rows" to "10", "cols" to "80", "id" to "directions", "oninput" to "render()")
            )
            .addBodyText(recipe.directions).closeBodyElement().addBreak()
            // add line break button
            .addBodyElement(
                "input",
                mapOf(
                    "type" to "button",
                    "value" to Preferences.getLanguageString("Add Line Break"),
                    "onclick" to "addTextBreak()"
                ),
                true
            )
            .addWhitespace()
            // add link button
            .addBodyElement(
                "input",
                mapOf(
                    "type" to "button",
                    "value" to Preferences.getLanguageString("Add Recipe Link"),
                    "onclick" to "showChooser()"
                ),
                true
            )
            .addBreak()
            .addBodyElement(
                "input",
                mapOf(
                    "type" to "button",
                    "value" to Preferences.getLanguageString("Start List"),
                    "onclick" to "startList()",
                    "id" to "startList"
                ),
                true
            )
            .addBodyElement(
                "input",
                mapOf(
                    "type" to "button",
                    "value" to Preferences.getLanguageString("End List"),
                    "onclick" to "endList()",
                    "id" to "endList",
                    "class" to "hidden"
                ),
                true
            )
            .addWhitespace()
            .addBodyElement(
                "input",
                mapOf(
                    "type" to "button",
                    "value" to Preferences.getLanguageString("Add List Item"),
                    "onclick" to "addListItem()",
                    "id" to "startItem"
                ),
                true
            )
            .addBodyElement(
                "input",
                mapOf(
                    "type" to "button",
                    "value" to Preferences.getLanguageString("End List Item"),
                    "onclick" to "endListItem()",
                    "id" to "endItem",
                    "class" to "hidden"
                ),
                true
            )

        // modal "add recipe link" area
        html.addBodyElement("div", mapOf("class" to "modal", "id" to "recipeChooser"))
            .addBodyElement("div", mapOf("class" to "modal-content"))
        html.addBodyText(HtmlElements.chooseRecipeModal())
            .addBodyElement(
                "input",
                mapOf(
                    "type" to "button",
                    "onclick" to "addLink(choice)",
                    "value" to Preferences.getLanguageString("Add Recipe Link")
                )
            ).closeBodyElement()
            .addBodyElement(
                "input",
                mapOf(
                    "type" to "button",
                    "onclick" to "closeChooser()",
                    "value" to Preferences.getLanguageString("Cancel")
                )
            ).closeBodyElement()
            .closeBodyElement()
            .closeBodyElement()

        html.closeBodyElement()
            .startCell(mapOf("class" to "big-arrow")).addBodyText("&rarr;").closeBodyElement()
            .startCell(mapOf("id" to "render-directions", "class" to "rendered")) // rendered
            .closeBodyElement()
            .closeBodyElement() // row
            .closeBodyElement() // table

        return ResponseEntity.ok(html.get())
    }

    /**
     * Saves the [Recipe] on the screen, in response to a click of the Save
     * button. The JavaScript code builds a [RecipeToSave] object from the
     * screen contents, and passes that in the request.
     *
     * For an existing [Recipe], check whether any [Ingredient]s have been
     * deleted; if so, they are deleted from the database. Existing (and
     * remaining) [Ingredient]s are just written over, if needed. New
     * [Ingredient]s are created.
     */
    @Operation(summary = "Validates and saves the current recipe")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The recipe has been updated",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")],
        ),
        ApiResponse(
            responseCode = "422",
            description = "There are problems with the recipe",
            content = [Content(mediaType = "application/json")],
        )
    ])
    @Transactional
    @PostMapping("/save-recipe", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
    fun saveRecipe(@RequestBody recipe: RecipeToSave): ResponseEntity<Recipe> {
        val errors = checkErrors(recipe)
        if (errors.isNotEmpty()) {
            val jsonErrors: String = Json.encodeToString(errors)
            throw ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, jsonErrors)
        }
        val categoryOptional = categoryDao.findByName(recipe.category)
        val category = when {
            categoryOptional.isEmpty -> {
                val category = Category(recipe.category)
                categoryDao.save(category)
            }

            else -> categoryOptional.get()
        }
        val dbRecipe = Recipe(recipe.name, recipe.directions, recipe.servings, category.id!!, recipe.id)
        val checkRemoved = recipe.id != null

        val recipeID = recipeDao.save(dbRecipe).id!!
        val ingredients = ArrayList<Ingredient>()
        recipe.ingredients.forEach {
            val foodOptional = foodItemDao.findByName(it.name)
            val foodId = when {
                foodOptional.isEmpty -> {
                    val foodItem = FoodItem(it.name)
                    foodItemDao.save(foodItem).id
                }

                else -> foodOptional.get().id
            }

            val ingredient = Ingredient(foodId!!, it.amount, it.unit, recipeID)
            ingredients.add(ingredient)
        }

        ingredients.forEach {
            val existing = ingredientDao.findByRecipeIdAndItemId(it.recipeId, it.itemId)
            if (existing.isPresent) {
                it.id = existing.get().id
            }
            ingredientDao.save(it)
        }
        if (checkRemoved) {
            checkDeletedIngredients(dbRecipe, ingredients)
        }

        return ResponseEntity.ok(dbRecipe)
    }

    /**
     * Checks for certain error conditions: no [Ingredient]s, no [Recipe] name,
     * and unnamed [Ingredient]s.
     *
     */
    private fun checkErrors(recipe: RecipeToSave): Errors {
        val list = mutableListOf<String>()
        if (recipe.name.isEmpty()) {
            list.add(Preferences.getLanguageString(NO_RECIPE_NAME))
        }
        if (recipe.servings < 1) {
            list.add(Preferences.getLanguageString(NO_SERVINGS))
        }
        if (recipe.category.isBlank()) {
            list.add(Preferences.getLanguageString(NO_CATEGORY))
        }
        val foundIngredient = fun(): Boolean {
            recipe.ingredients.forEach {
                if (it.name.isNotEmpty()) {
                    return true
                }
            }
            return false
        }
        if (!foundIngredient()) {
            list.add(Preferences.getLanguageString(NO_INGREDIENTS))
        }

        return Errors(list)
    }

    /**
     * Go through the list of [Ingredient]s. For each, if it previously
     * existed in the [Recipe], remove it from the database.
     */
    private fun checkDeletedIngredients(dbRecipe: Recipe, ingredients: List<Ingredient>) {
        val recipeId = dbRecipe.id!!
        val previous = ingredientDao.findAllByRecipeId(recipeId)
        ingredients.forEach {
            if (previous.contains(it)) {
                previous.remove(it)
            }
        }

        // previous now contains a list of ingredients to be deleted
        ingredientDao.deleteAll(previous)
    }

    /**
     * Returns an HTML link to another recipe, suitable for placement within the current recipe.
     */
    @Operation(summary = "Gets an HTML link suitable for adding to the current recipe")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Link to the recipe",
            content = [Content(mediaType = "text/plain; charset=UTF-8")]),
    )
    @GetMapping("/getRecipeLink/{recipeId}", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun getRecipeLink(@PathVariable("recipeId") recipeId: Long): ResponseEntity<String> {
        return ResponseEntity.ok(HtmlElements.addRecipeLink(recipeId))
    }

    /**
     * Object that supplies some needed (private) constants.
     */
    companion object {
        /**
         * Error strings
         */
        internal const val NO_RECIPE_NAME = "Recipe must have a name."
        internal const val NO_SERVINGS = "Recipe must make at least 1 serving."
        internal const val NO_INGREDIENTS = "No ingredients found."
        internal const val NO_CATEGORY = "No category set."
    }
}
