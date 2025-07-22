/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.display

import java.util.Collections.singletonMap
import xyz.jimh.souschef.config.Preferences
import xyz.jimh.souschef.config.SpringContext
import xyz.jimh.souschef.control.CategoryController
import xyz.jimh.souschef.control.RecipeController
import xyz.jimh.souschef.display.HtmlElements.TABLE_NAME

/**
 * Singleton object to add certain HTML elements to a recipe's "directions" field.
 */
object HtmlElements {
    /**
     * Name for an HTML table created for the edit screen.
     */
    const val TABLE_NAME = "ingredients-table"

    private lateinit var recipeController: RecipeController
    private lateinit var categoryController: CategoryController

    /**
     * Given a [recipeId], returns an anchor-link to that recipe, sutiable for
     * placing in a different, related recipe.
     */
    fun addRecipeLink(recipeId: Long): String {
        loadControllers()
        val recipe = recipeController.getRecipe(recipeId)
        return "<a href='/souschef/show-recipe/$recipeId'>${recipe.name}</a>"
    }

    /**
     * Creates a modal window to let the user choose a recipe to link to the
     * one currently being edited.
     */
    fun chooseRecipeModal(): String {
        loadControllers()

        val builder = StringBuilder()
        builder.append("<span class='close' onclick='closeChooser()'>&times;</span>\n")
        val categories = categoryController.findAll()
        categories.forEach { category ->
            run {
                val id = category.id
                if (id != null) {
                    builder.append("<details>\n<summary>${category.name}</summary>\n<ul>\n")
                    val recipes = recipeController.getRecipes(id)
                    recipes.forEach { recipe ->
                        run {
                            builder.append("<li class='list-item' " +
                                    "onclick='chooseRecipe(this, ${recipe.id})'>${recipe.name}</li>\n")
                        }
                    }
                    builder.append("</ul>\n</details>\n")
                }
            }
        }

        return builder.toString()
    }

    private fun loadControllers() {
        if (!this::recipeController.isInitialized) {
            recipeController = SpringContext.getBean(RecipeController::class.java)
        }
        if (!this::categoryController.isInitialized) {
            categoryController = SpringContext.getBean(CategoryController::class.java)
        }
    }

    /**
     * Builds a category selector in the given [html]. If [catName] is given,
     * and matches a stored category name, that value is selected.
     */
    fun addCategorySelector(html: HtmlBuilder, catName: String = "") {
        html.addBodyElement("label", singletonMap("for", "category"))
            .addBodyText(Preferences.getLanguageString("category"))
            .closeBodyElement()
        html.addBodyText(IngredientBuilder.buildCategorySelector("category", catName))
            .addBreak().addBreak()
    }

    /**
     * Builds common HTML to display a list of ingredients for editing. In particular,
     * this function starts the table and builds column headers.
     */
    fun startEditIngredientsTable(html: HtmlBuilder) {
        html.startTable(mapOf("id" to TABLE_NAME))
            .startRow()
            .startHeadingCell(mapOf("class" to "tableHeader", "colspan" to "4"))
            .addBodyText(Preferences.getLanguageString("Ingredients")).addWhitespace()
            // TODO enable sorting
            .addBodyElement(
                tag = "input",
                attributes = mapOf(
                    "type" to "button",
                    "id" to "sort-ingredients",
                    "onclick" to "toggleSortIngredients()",
                    "value" to "Sort"
                ),
                closing = true
            )
            .addBodyElement(
                tag = "input",
                attributes = mapOf("type" to "hidden", "id" to "toggle-state", "value" to "off"),
                closing = true
            )
            .closeBodyElement()
            .closeBodyElement() // title row
            .startRow()
            .startHeadingCell().addBodyText(Preferences.getLanguageString("Amount")).closeBodyElement()
            .startHeadingCell().addBodyText(Preferences.getLanguageString("Unit")).closeBodyElement()
            .startHeadingCell().addBodyText(Preferences.getLanguageString("Ingredient")).closeBodyElement()
            .closeBodyElement()
    }

}