/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.control

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import xyz.jimh.souschef.data.Ingredient
import xyz.jimh.souschef.data.IngredientDao
import xyz.jimh.souschef.data.Recipe

/**
 * Controller managing retrieval of [Ingredient]s for various [Recipe]s.
 * @constructor Automagically built with an [IngredientDao]
 */
@RestController
class IngredientController(private val ingredientDao: IngredientDao) {
    /**
     * Retrieves all [Ingredient]s for all [Recipe]s.
     */
    @GetMapping("/ingredients")
    fun getIngredients(): List<Ingredient> {
        return ingredientDao.findAll()
    }

    /**
     * Retrieves a single [Ingredient] by its unique [id].
     */
    @GetMapping("/ingredients/{id}")
    fun getIngredient(@PathVariable id: Long): Ingredient {
        val optional = ingredientDao.findById(id)
        check(optional.isPresent) { "Could not find ingredient: $id" }
        return optional.get()
    }

    /**
     * Retrieves a list of [Ingredient]s for a particular [Recipe], using
     * the [recipeId].
     */
    @GetMapping("/ingredients/{recipeId}/inventory")
    fun getIngredientInventory(@PathVariable recipeId: Long): List<Ingredient> {
        return ingredientDao.findAllByRecipeId(recipeId)
    }

    /**
     * Creates a new [Ingredient] record and saves it in the database.
     */
    @PostMapping("/ingredients")
    fun addIngredient(@RequestBody ingredient: Ingredient): Ingredient {
        return ingredientDao.save(ingredient)
    }

    /**
     * Changes an existing [Ingredient] and saves the changes in the database.
     */
    @PutMapping("/ingredients")
    fun updateIngredient(@RequestBody ingredient: Ingredient): Ingredient {
        return ingredientDao.save(ingredient)
    }
}