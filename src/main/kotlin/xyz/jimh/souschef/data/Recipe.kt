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
import java.time.Instant

/**
 * A recipe; a record in the "recipes" table.
 *
 * @property name
 * @property directions How to stir, etc.
 * @property servings How many servings does the recipe provide
 * @property categoryId ID of the recipe's [Category]
 * @property id Unique ID
 * @property deleted true if the recipe is marked deleted
 * @property deletedOn date/time that the recipe was deleted, or null if not deleted
 */
@Schema(description = "Description of the recipe")
@Entity(name = "recipes")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
data class Recipe(
    @Schema(description = "Name of the recipe", example = "Apple Pie")
    var name: String,
    @Schema(description = "Recipe directions", example = "Mix, stir, etc.", required = false)
    var directions: String,
    @Schema(description = "How many servings in the original", example = "3", required = true)
    var servings: Int,
    @Schema(description = "Category for the recipe", example = "1", required = true)
    var categoryId: Long,
    @Schema(description = "Recipe ID, initially assigned by the database", example = "12", required = false)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @Schema(description = "Whether the recipe is deleted", required = false)
    var deleted: Boolean = false,
    @Schema(description = "Date/time the recipe was deleted, or null", example = "2025-03-29T12:34:56", required = false)
    var deletedOn: Instant? = null
)