/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.control

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import java.time.Instant
import org.springframework.http.MediaType
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
    @Operation(summary = "Add a recipe to the database")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The new recipe",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @PostMapping(
        "/recipes",
        consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
    )
    fun addRecipe(@RequestBody recipe: Recipe) : Recipe {
        return recipeDao.save(recipe)
    }

    /**
     * Get all [Recipe]s not marked deleted.
     */
    @Operation(summary = "Get all recipes that are not marked 'deleted.'")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The list of recipes.",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping("/recipes", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
    fun getRecipes() : List<Recipe> {
        return recipeDao.findAllByDeletedIsFalse()
    }

    /**
     * Get the [Recipe] with the given [id].
     * @throws IllegalStateException if not found.
     */
    @Operation(summary = "Get a recipe by its ID")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The requested recipe",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping("/recipes/{id}", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
    fun getRecipe(@PathVariable("id") id : Long) : Recipe {
        val recipe = recipeDao.findById(id)
        check(recipe.isPresent) { "Recipe with id $id not found" }
        return recipe.get()
    }

    /**
     * Get the [Recipe] with the given [name].
     * @throws IllegalStateException if not found.
     */
    @Operation(summary = "Get a recipe by its name")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The requested recipe",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping(
        "/recipes/name/{name}",
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE]
    )
    fun getRecipe(@PathVariable("name") name : String) : Recipe {
        val recipe = recipeDao.findByName(name)
        check(recipe.isPresent) { "Recipe with name $name not found" }
        return recipe.get()
    }

    /**
     * Get all [Recipe]s (except those marked as deleted) in the given
     * [category] id.
     */
    @Operation(summary = "Get all recipes that are not marked 'deleted' in the given category.")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The list of recipes",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping(
        "/recipes/category/{category}",
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE]
    )
    fun getRecipes(@PathVariable("category") category : Long) : List<Recipe> {
        return recipeDao.findAllByCategoryIdAndDeletedIsFalse(category)
    }

    /**
     * Save the (existing) [Recipe] with its changes.
     */
    @Operation(summary = "Save changes to a recipe.")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The modified recipe",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @PutMapping(
        "/recipes",
        consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
    )
    fun updateRecipe(@RequestBody recipe: Recipe) : Recipe {
        return recipeDao.save(recipe)
    }

    /**
     * Mark the [Recipe] with the given [id] as deleted.
     */
    @Operation(summary = "Delete a recipe by its ID")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The 'deleted' recipe",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @DeleteMapping("/recipes/{id}", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
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
    @Operation(summary = "Update a recipe by its name")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The modified recipe",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @DeleteMapping(
        "/recipes/name/{name}",
        consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
    )
    fun deleteRecipe(@PathVariable("name") name : String) : Recipe {
        val optional = recipeDao.findByName(name)
        check(optional.isPresent) { "Recipe with name: $name not found" }
        val recipe = optional.get()
        recipe.deleted = true
        recipe.deletedOn = Instant.now()
        return recipeDao.save(recipe)
    }
}