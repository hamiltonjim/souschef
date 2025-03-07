/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

/*
* Grab all data from the table, and build an object:
* Recipe id
* Recipe name
* Category
* Servings
* Ingredients array
*
* Ingredient is
* Item name
* Amount
* Unit (may be null or blank)
* */
data class RecipeToSave(
    val id: Long?,
    val name: String,
    val category: String,
    val servings: Int,
    val directions: String,
    val ingredients: List<IngredientToSave>,
)
