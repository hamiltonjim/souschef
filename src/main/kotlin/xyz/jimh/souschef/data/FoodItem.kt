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
 * Class describing any item that may be an ingredient for a recipe. Corresponds
 * to the "foods" table.
 *
 * @property name
 * @property description
 * @property notes
 * @property id
 */
@Suppress("unused")
@Entity(name = "foods")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
data class FoodItem(
    var name: String,
    var description: String? = null,
    var notes: String? = null,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
)