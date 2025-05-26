/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An [Ingredient] that is about to be saved with a [Recipe]
 * @property name food name
 * @property amount amount used
 * @property unit name of the unit
 * @property type type of the unit (WEIGHT or VOLUME)
 */
@Schema(description = "An ingredient to be saved with a recipe")
data class IngredientToSave(
    @field:Schema(description = "The ingredient's name", example = "sugar")
    val name: String,
    @field:Schema(description = "The amount of the ingredient to use", example = "1")
    val amount: Double,
    @field:Schema(description = "The unit for the amount", example = "cup")
    val unit: String,
    @field:Schema(description = "The type of the unit", example = "VOLUME")
    val type: String
)
