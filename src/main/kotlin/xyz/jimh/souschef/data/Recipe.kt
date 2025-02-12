// Copyright © 2025 Jim Hamilton. All rights reserved.

package xyz.jimh.souschef.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.Instant

@Entity(name = "recipes")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
class Recipe(
    var name: String,
    var directions: String,
    var servings: Int,
    var categoryId: Long,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    var deleted: Boolean = false,
    var deletedOn: Instant? = null
)
