/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.display

import xyz.jimh.souschef.config.SpringContext
import xyz.jimh.souschef.control.CategoryController
import xyz.jimh.souschef.control.RecipeController

/**
 * Singleton object to add certain HTML elements to a recipe's "directions" field.
 */
object HtmlElements {

    private lateinit var recipeController: RecipeController
    private lateinit var categoryController: CategoryController

    fun addRecipeLink(recipeId: Long): String {
        loadControllers()
        val recipe = recipeController.getRecipe(recipeId)
        return "<a href='/souschef/show-recipe/$recipeId'>${recipe.name}</a>"
    }

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
}