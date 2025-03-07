/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.control

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import xyz.jimh.souschef.data.Category
import xyz.jimh.souschef.data.CategoryCount
import xyz.jimh.souschef.data.CategoryDao
import xyz.jimh.souschef.data.CountDao

@RestController
class CategoryController(val categoryDao: CategoryDao, val countDao: CountDao) {
    @Operation(summary = "Create a new category")
    @PostMapping("/categories")
    fun create(@RequestBody category: Category): Category {
        return categoryDao.save(category)
    }

    @Operation(summary = "Get all categories")
    @GetMapping("/categories")
    fun findAll(): List<Category> {
        return categoryDao.findAll()
    }

    @Operation(summary = "Get category by id")
    @GetMapping("/categories/{id}")
    fun findById(@PathVariable("id") id: Long): Category? {
        return categoryDao.getReferenceById(id)
    }

    @GetMapping("/category-counts/{includeDeleted}")
    fun countByCategory(@PathVariable(
        name = "includeDeleted",
        required = false
    ) includeDeleted: Boolean?): List<CategoryCount> {
        val include = includeDeleted ?: false
        return countDao.getCategoryCounts(include)
    }
}