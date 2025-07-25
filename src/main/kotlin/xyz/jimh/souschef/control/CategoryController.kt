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
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import xyz.jimh.souschef.data.Category
import xyz.jimh.souschef.data.CategoryCount
import xyz.jimh.souschef.data.CategoryDao
import xyz.jimh.souschef.data.CountDao
import xyz.jimh.souschef.data.Recipe

/**
 * Controller for [Category] objects.
 * @property categoryDao accessor for categories table
 * @property countDao accessor for counting recipes by category
 */
@RestController
class CategoryController(val categoryDao: CategoryDao, val countDao: CountDao) {
    /**
     * Creates a new [Category] with the JSON object passed in ([category]) and
     * saves it in the database.
     */
    @Operation(summary = "Create a new category")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Category list",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @PostMapping(
        "/categories",
        consumes = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE]
    )
    fun create(@RequestBody category: Category): Category {
        return categoryDao.save(category)
    }

    /**
     * Returns a list of [Category] objects stored in the database.
     */
    @Operation(summary = "Get all categories")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Category list",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping(
        "/categories",
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE]
    )
    fun findAll(): List<Category> {
        return categoryDao.findAll().sortedBy(Category::name)
    }

    /**
     * Gets the [Category] with the given [id].
     */
    @Operation(summary = "Get category by id")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "The Category",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping(
        "/categories/{id}",
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE]
    )
    fun findById(@PathVariable("id") id: Long): Optional<Category> {
        return categoryDao.findById(id)
    }

    /**
     * Gets the number of [Recipe]s in each [Category]; optionally,
     * include deleted recipes, if [includeDeleted] is true.
     */
    @Operation(summary = "Get a count of all recipes in each category")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Category list with recipe counts",
            content = [Content(mediaType = "application/json"), Content(mediaType = "application/xml")]
        ),
    ])
    @GetMapping(
        value = ["/category-counts/{includeDeleted}", "/category-counts"],
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE]
    )
    fun countByCategory(@PathVariable(
        name = "includeDeleted",
        required = false,
    ) includeDeleted: Optional<Boolean>): List<CategoryCount> {
        val include = when {
            includeDeleted.isPresent -> includeDeleted.get()
            else -> false
        }
        return countDao.getCategoryCounts(include)
    }
}