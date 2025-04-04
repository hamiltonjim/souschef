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
/**
 * A recipe that is about to be saved.
 * @property id unique ID; null if this is a new [Recipe]
 * @property name name of the [Recipe]
 * @property category name of the [Category]; will be converted to an ID
 * @property servings number of servings the [Recipe] yields
 * @property directions how to stir, etc.
 * @property ingredients List of [IngredientToSave]
 */
data class RecipeToSave(
    val id: Long?,
    val name: String,
    val category: String,
    val servings: Int,
    val directions: String,
    val ingredients: List<IngredientToSave>,
)
