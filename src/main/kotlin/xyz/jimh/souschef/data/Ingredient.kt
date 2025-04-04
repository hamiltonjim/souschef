/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
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
@Entity(name = "ingredients")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
data class Ingredient(
    var itemId: Long,
    var amount: Double,
    var unit: String?,
    var recipeId: Long,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
)