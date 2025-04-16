/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.Hidden
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Interface to the "ingredients" table.
 */
@Hidden     // from Swagger
interface IngredientDao : JpaRepository<Ingredient, Long> {
    /**
     * Returns a [MutableList] of all [Ingredient]s in a [Recipe].
     */
    fun findAllByRecipeId(recipeId: Long): MutableList<Ingredient>

    /**
     * Returns an [Ingredient] in a particular [Recipe] if it exists.
     */
    fun findByRecipeIdAndItemId(recipeId: Long, itemId: Long): Optional<Ingredient>
}