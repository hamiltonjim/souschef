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
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import xyz.jimh.souschef.data.FoodItem
import xyz.jimh.souschef.data.FoodItemDao

/**
 * Controller for getting and saving [FoodItem]s.
 * @constructor Automagically built with a [FoodItemDao].
 */
@RestController
class FoodController(private val foodItemDao: FoodItemDao) {

    /**
     * Enables logging for this controller
     */
    val logger = KotlinLogging.logger {}

    /**
     * Saves a [FoodItem] in the database, passed in as [food].
     */
    @Operation(summary = "Add a food item")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Created item",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @PostMapping(
        "/foods",
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE]
    )
    fun postFood(@RequestBody food: FoodItem): FoodItem {
        return foodItemDao.save(food)
    }

    /**
     * Finds a [FoodItem] by its [id].
     */
    @Operation(summary = "Get a food item by its id")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The requested food item",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping("/foods/{id}", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
    fun getFood(@PathVariable id: Long): Optional<FoodItem> {
        return foodItemDao.findById(id)
    }

    /**
     * Gets all [FoodItem]s in the database.
     */
    @Operation(summary = "Gets all food items in the database")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "list of all foods in the database",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping("/foods", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
    fun getAllFood(): List<FoodItem> {
        return foodItemDao.findAll()
    }
}