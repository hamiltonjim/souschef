/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

/**
 * An ingredient in a recipe. Corresponds to the ingredients table.
 * @property itemId the ID of the [FoodItem]
 * @property amount how much to use
 * @property unit the unit of measurement; null if the item is just an item (an egg, for example)
 * @property recipeId ID of the [Recipe] this ingredient belongs to
 * @property id unique ID
 */
@Schema(description = "Any ingredient for any recipe")
@Entity(name = "ingredients")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
data class Ingredient(
    @Schema(description = "Id of the food item", example = "42")
    var itemId: Long,
    @Schema(description = "How much to use", example = "3")
    var amount: Double,
    @Schema(description = "The unit for the amount", example = "ounces", required = false)
    var unit: String?,
    @Schema(description = "ID of the recipe this is used in", example = "12", required = true)
    var recipeId: Long,
    @Schema(description = "ID of this ingredient, assigned by the database", example = "69")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
)