/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.Hidden
import java.util.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

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

    /**
     * Finds all [Recipe]s with the partial [title].
     */
    @Query(
        value = "select * from recipes r where r.name ilike '%' || :title || '%' and not r.deleted",
        nativeQuery = true,
    )
    fun findAllWithPartialTitle(title: String): List<Recipe>

    /**
     * Finds all [Recipe]s with the given [ingredient] (full or partial).
     */
    @Query(
        value = "select distinct r.* from recipes r " +
                "join ingredients i on i.recipe_id = r.id " +
                "join foods f on f.id = i.item_id " +
                "where f.name ilike '%' || :ingredient || '%' ",
        nativeQuery = true,
    )
    fun findAllWithIngredient(ingredient: String): List<Recipe>

    /**
     * Finds all [Recipe]s with the given [word] or phrase in their directions.
     */
    @Query(
        value = "select * from recipes r where r.directions ilike '%' || :word || '%'",
        nativeQuery = true,
    )
    fun findAllWithWordInDirections(word: String): List<Recipe>
}