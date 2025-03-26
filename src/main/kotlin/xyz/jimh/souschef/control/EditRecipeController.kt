/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.control

import jakarta.servlet.http.HttpServletRequest
import java.util.Collections.singletonMap
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import xyz.jimh.souschef.config.Listener
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.data.Category
import xyz.jimh.souschef.data.CategoryDao
import xyz.jimh.souschef.data.Errors
import xyz.jimh.souschef.data.FoodItem
import xyz.jimh.souschef.data.FoodItemDao
import xyz.jimh.souschef.data.Ingredient
import xyz.jimh.souschef.data.IngredientDao
import xyz.jimh.souschef.data.Recipe
import xyz.jimh.souschef.data.RecipeDao
import xyz.jimh.souschef.data.RecipeToSave
import xyz.jimh.souschef.display.HtmlBuilder
import xyz.jimh.souschef.display.IngredientBuilder
import xyz.jimh.souschef.display.ResourceText

@RestController
class EditRecipeController(
    private val categoryDao: CategoryDao,
    private val recipeDao: RecipeDao,
    private val foodItemDao: FoodItemDao,
    private val ingredientDao: IngredientDao,
) : Listener {

    val kLogger = KotlinLogging.logger {}

    companion object {
        private const val TABLE_NAME = "ingredients-table"
    }

    override fun listen(name: String, value: Any) {
        kLogger.debug { "listen: $name=$value" }
    }

    @GetMapping("/edit-recipe/{recipeId}")
    fun editRecipe(
        request: HttpServletRequest,
        @PathVariable("recipeId") recipeId: Long
    ): ResponseEntity<String> {
        val html = Preferences.initHtml()
        val recipeOptional = recipeDao.findById(recipeId)
        if (recipeOptional.isEmpty) {
            return ResponseEntity.notFound().build()
        }
        return doEditRecipe(request, recipeOptional.get(), html)
    }

    @GetMapping("/new-recipe/{categoryId}")
    fun newRecipe(request: HttpServletRequest, @PathVariable categoryId: Long): ResponseEntity<String> {
        val html = Preferences.initHtml()
        val recipe = Recipe("", "", 0, categoryId)
        return doEditRecipe(request, recipe, html)
    }

    @Transactional
    @PostMapping("/save-recipe")
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

    private fun checkErrors(recipe: RecipeToSave): Errors {
        val list = mutableListOf<String>()
        if (recipe.name.isEmpty()) {
            list.add("Recipe must have a name.")
        }
        if (recipe.servings < 1) {
            list.add("Recipe must make at least 1 serving.")
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
            list.add("No ingredients found.")
        }

        return Errors(list)
    }

    private fun checkDeletedIngredients(dbRecipe: Recipe, ingredients: java.util.ArrayList<Ingredient>) {
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

    private fun doEditRecipe(
        request: HttpServletRequest,
        recipe: Recipe,
        html: HtmlBuilder
    ): ResponseEntity<String> {
        val remoteHost = request.remoteHost
        html.addHeaderWhitespace().addHeaderElement("style")
            .addHeaderWhitespace().addHeaderText(ResourceText.get("editor.css"))
            .addHeaderWhitespace().closeHeaderElement()

        val ingredients = when {
            recipe.id == null -> emptyList()
            else -> {
                val recipeId = recipe.id!!
                ingredientDao.findAllByRecipeId(recipeId)
            }
        }

        html.addBodyElement("label", singletonMap("for", "category")).addBodyText("Category: ")
            .closeBodyElement()
        val id = recipe.categoryId
        val category = categoryDao.findById(id)
        val catName = when {
            category.isPresent -> category.get().name
            else -> ""
        }
        html.addBodyText(IngredientBuilder.buildCategorySelector("category", catName))
            .addBreak().addBreak()

        html.addBodyElement("label", mapOf("class" to "title", "for" to "recipe-title")).addBodyText("Title: ")
            .closeBodyElement()

        html.addBodyElement(
            "input",
            mapOf(
                "id" to "recipe-title",
                "type" to "text",
                "value" to recipe.name,
                "placeholder" to "Type recipe title here…"
            ),
            true
        )

        // save button
        html.addWhitespace().addBodyElement(
            "input",
            mapOf("class" to "title", "value" to "Save", "type" to "button", "onclick" to "doSave('$TABLE_NAME')"),
            true
        )
            .addBreak().addBreak()

        html.addBodyElement("label", singletonMap("for", "serves"))
            .addBodyText("Serves: ").closeBodyElement()

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
            "input", mapOf(
                "type" to "hidden", "id" to "recipe-id", "value" to when {
                    recipe.id == null -> "null"
                    else -> "${recipe.id}"
                }
            ), true
        )

        // Ingredient list
        html.startTable(singletonMap("id", TABLE_NAME))
            .startRow()
            .startHeadingCell(mapOf("class" to "tableHeader", "colspan" to "4")).addBodyText("Ingredients")
            .closeBodyElement()
            .closeBodyElement() // title row
            .startRow()
            .startHeadingCell().addBodyText("Amount").closeBodyElement()
            .startHeadingCell().addBodyText("Unit").closeBodyElement()
            .startHeadingCell().addBodyText("Ingredient").closeBodyElement()
            .closeBodyElement() // heading row

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
            html.startRow()

                .startCell()
                .addBodyText(IngredientBuilder.buildAmountInput("amount-$counter", it.amount))
                .closeBodyElement()

                .startCell().addBodyText(IngredientBuilder.buildUnitSelector(remoteHost, "unit-$counter", it.unit))
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
            mapOf("type" to "button", "value" to "Add Ingredient", "onclick" to "insertNewRow('$TABLE_NAME')"),
            true
        ).addBreak().addBreak()

        html.addBodyElement("textArea", mapOf("rows" to "10", "cols" to "80", "id" to "directions"))
            .addBodyText(recipe.directions).closeBodyElement().addBreak()
        html.addBodyElement(
            "input",
            mapOf(
                "type" to "button",
                "value" to "Add Line Break",
                "onclick" to "addLineBreak()"
            )
        ).addBreak()

        return ResponseEntity.ok(html.get())
    }
}