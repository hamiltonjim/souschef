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
 * Class describing any item that may be an ingredient for a recipe. Corresponds
 * to the "foods" table.
 *
 * @property name the name of the food
 * @property description a description of the food; may be null
 * @property notes any required notes for the food; may be null
 * @property id the food item's unique ID
 */
@Schema(description = "Any food item used as an ingredient")
@Entity(name = "foods")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
data class FoodItem(
    @Schema(description = "Name of the food item", example = "sugar")
    var name: String,
    @Schema(description = "Description of food item", example = "food", required = false)
    var description: String? = null,
    @Schema(description = "Any notes about the item", example = "how to use", required = false)
    var notes: String? = null,
    @Schema(description = "ID of food item, initially assigned by the database", example = "42", required = false)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
)