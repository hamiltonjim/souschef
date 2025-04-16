/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.Hidden
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Interface to the recipes table in the database.
 */
@Hidden     // from Swagger
interface RecipeDao : JpaRepository<Recipe, Long> {
    /**
     * Find [Recipe] by its name.
     */
    fun findByName(name: String): Optional<Recipe>

    /**
     * Find all [Recipe]s that are not marked deleted.
     */
    fun findAllByDeletedIsFalse(): List<Recipe>

    /**
     * Find all [Recipe]s with the given [Category] ID that are not marked deleted.
     */
    fun findAllByCategoryIdAndDeletedIsFalse(category: Long): List<Recipe>

    /**
     * Find all [Recipe]s with the given [Category] ID
     */
    fun findAllByCategoryId(categoryId: Long): List<Recipe>
}