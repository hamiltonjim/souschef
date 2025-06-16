/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.control

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import java.util.Optional
import org.springframework.http.MediaType
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
    @Operation(summary = "Get all ingredients for all recipes")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The complete list of ingredients for all recipes",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping(
        "/ingredients",
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE]
    )
    fun getIngredients(): List<Ingredient> {
        return ingredientDao.findAll()
    }

    /**
     * Retrieves a single [Ingredient] by its unique [id].
     */
    @Operation(summary = "Get a single ingredient by its unique ID")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The requested ingredient",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping(
        "/ingredients/{id}",
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE]
    )
    fun getIngredient(@PathVariable id: Long): Ingredient {
        val optional = ingredientDao.findById(id)
        check(optional.isPresent) { "Could not find ingredient: $id" }
        return optional.get()
    }

    /**
     * Retrieves a list of [Ingredient]s for a particular [Recipe], using
     * the [recipeId].
     */
    @Operation(summary = "Get all ingredients for the given recipe ID")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The requested ingredients for the recipe ID",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping(
        "/ingredients/{recipeId}/inventory",
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE]
    )
    fun getIngredientInventory(@PathVariable recipeId: Long): List<Ingredient> {
        return ingredientDao.findAllByRecipeIdOrderBySortIndex(recipeId)
    }

    /**
     * Retrieves an ingredient by its [recipeId] and sort [index].
     */
    @Operation(summary = "Get a single ingredient by its recipe ID and sort index")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The requested ingredient",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        )
    ])
    @GetMapping(
        "/ingredients/{recipeId}/{index}",
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE]
    )
    fun getIngredientByRecipeAndIndex(
        @PathVariable recipeId: Long,
        @PathVariable index: Int
    ): Optional<Ingredient> {
        return ingredientDao.findByRecipeIdAndSortIndex(recipeId, index)
    }

    /**
     * Creates a new [Ingredient] record and saves it in the database.
     */
    @Operation(summary = "Add a single ingredient to the database, for use by any recipe")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The new ingredient",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @PostMapping(
        "/ingredients",
        consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
    )
    fun addIngredient(@RequestBody ingredient: Ingredient): Ingredient {
        return ingredientDao.save(ingredient)
    }

    /**
     * Changes an existing [Ingredient] and saves the changes in the database.
     */
    @Operation(summary = "Modify a single ingredient for a recipe")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The modified ingredient",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @PutMapping(
        "/ingredients",
        consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE]
    )
    fun updateIngredient(@RequestBody ingredient: Ingredient): Ingredient {
        return ingredientDao.save(ingredient)
    }
}