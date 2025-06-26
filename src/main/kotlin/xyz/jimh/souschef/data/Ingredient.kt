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
 * @property sortIndex where this ingredient goes in the list
 */
@Schema(description = "Any ingredient for any recipe")
@Entity(name = "ingredients")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
data class Ingredient(
    @field:Schema(description = "Id of the food item", example = "42")
    var itemId: Long,
    @field:Schema(description = "How much to use", example = "3")
    var amount: Double,
    @field:Schema(description = "The unit for the amount", example = "ounces", required = false)
    var unit: String?,
    @field:Schema(description = "ID of the recipe this is used in", example = "12", required = true)
    var recipeId: Long,
    @field:Schema(description = "ID of this ingredient, assigned by the database", example = "69")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
) {
    @field:Schema(description = "The order of this ingredient in the list", example = "1")
    var sortIndex: Int? = null     // NOT part of equals() or hashCode()

    constructor(
        itemId: Long,
        amount: Double,
        unit: String?,
        recipeId: Long,
        sortIndex: Int?,
        id: Long? = null,
    ) : this(itemId, amount, unit, recipeId, id) {
        this.sortIndex = sortIndex
    }
}