/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.media.Schema
/**
 * A recipe that is about to be saved.
 * @property id unique ID; null if this is a new [Recipe]
 * @property name name of the [Recipe]
 * @property category name of the [Category]; will be converted to an ID
 * @property servings number of servings the [Recipe] yields
 * @property directions how to stir, etc.
 * @property ingredients List of [IngredientToSave]
 */
@Schema(
    name = "Recipe To Save",
    description = "A recipe with its list of ingredients, to be saved in multiple database tables"
)
data class RecipeToSave(
    @Schema(description = "The ID of the recipe, if it's being updated", example = "42")
    val id: Long?,
    @Schema(description = "The name of the recipe to be updated", example = "Pumpkin Pie")
    val name: String,
    @Schema(description = "The category name of the recipe", example = "Desserts")
    val category: String,
    @Schema(description = "The number of servings in the original recipe", example = "8")
    val servings: Int,
    @Schema(description = "The cooking directions", example = "Stir, mix, etc")
    val directions: String,
    @Schema(description = "The list of ingredients")
    val ingredients: List<IngredientToSave>,
)
