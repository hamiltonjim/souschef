/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.control

import java.time.Instant
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import xyz.jimh.souschef.data.Recipe
import xyz.jimh.souschef.data.RecipeDao

/**
 * Direct controller for saving and getting [Recipe] records.
 * @constructor Automagically built with the [RecipeDao]
 */
@RestController
class RecipeController(private val recipeDao: RecipeDao) {

    /**
     * Save a new [Recipe] in the database.
     */
    @PostMapping("/recipes")
    fun addRecipe(@RequestBody recipe: Recipe) : Recipe {
        return recipeDao.save(recipe)
    }

    /**
     * Get all [Recipe]s not marked deleted.
     */
    @GetMapping("/recipes")
    fun getRecipes() : List<Recipe> {
        return recipeDao.findAllByDeletedIsFalse()
    }

    /**
     * Get the [Recipe] with the given [id].
     * @throws IllegalStateException if not found.
     */
    @GetMapping("/recipes/{id}")
    fun getRecipe(@PathVariable("id") id : Long) : Recipe {
        val recipe = recipeDao.findById(id)
        check(recipe.isPresent) { "Recipe with id $id not found" }
        return recipe.get()
    }

    /**
     * Get the [Recipe] with the given [name].
     * @throws IllegalStateException if not found.
     */
    @GetMapping("/recipes/name/{name}")
    fun getRecipe(@PathVariable("name") name : String) : Recipe {
        val recipe = recipeDao.findByName(name)
        check(recipe.isPresent) { "Recipe with name $name not found" }
        return recipe.get()
    }

    /**
     * Get all [Recipe]s (except those marked as deleted) in the given
     * [category] id.
     */
    @GetMapping("/recipes/category/{category}")
    fun getRecipes(@PathVariable("category") category : Long) : List<Recipe> {
        return recipeDao.findAllByCategoryIdAndDeletedIsFalse(category)
    }

    /**
     * Save the (existing) [Recipe] with its changes.
     */
    @PutMapping("/recipes")
    fun updateRecipe(@RequestBody recipe: Recipe) : Recipe {
        return recipeDao.save(recipe)
    }

    /**
     * Mark the [Recipe] with the given [id] as deleted.
     */
    @DeleteMapping("/recipes/{id}")
    fun deleteRecipe(@PathVariable("id") id : Long) : Recipe {
        val optional = recipeDao.findById(id)
        check(optional.isPresent) { "Recipe with id $id not found" }
        val recipe = optional.get()
        recipe.deleted = true
        recipe.deletedOn = Instant.now()
        return recipeDao.save(recipe)
    }

    /**
     * Mark the [Recipe] with the given [name] as deleted.
     */
    @DeleteMapping("/recipes/name/{name}")
    fun deleteRecipe(@PathVariable("name") name : String) : Recipe {
        val optional = recipeDao.findByName(name)
        check(optional.isPresent) { "Recipe with name: $name not found" }
        val recipe = optional.get()
        recipe.deleted = true
        recipe.deletedOn = Instant.now()
        return recipeDao.save(recipe)
    }
}