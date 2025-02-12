// Copyright © 2025 Jim Hamilton. All rights reserved.

package xyz.jimh.souschef.data

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository

interface IngredientDao : JpaRepository<Ingredient, Long> {
    fun findAllByRecipeId(recipeId: Long): MutableList<Ingredient>
    fun findByRecipeIdAndItemId(recipeId: Long, itemId: Long): Optional<Ingredient>
}