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
@Entity(name = "recipes")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
data class Recipe(
    var name: String,
    var directions: String,
    var servings: Int,
    var categoryId: Long,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    var deleted: Boolean = false,
    var deletedOn: Instant? = null
)