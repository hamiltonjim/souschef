// Copyright © 2025 Jim Hamilton. All rights reserved.

package xyz.jimh.souschef.control

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import xyz.jimh.souschef.data.Category
import xyz.jimh.souschef.data.CategoryDao

@RestController
class CategoryController(val categoryDao: CategoryDao) {
    @PostMapping("/categories")
    fun create(@RequestBody category: Category): Category {
        return categoryDao.save(category)
    }

    @GetMapping("/categories")
    fun findAll(): List<Category> {
        return categoryDao.findAll()
    }

    @GetMapping("/categories/{id}")
    fun findById(@PathVariable("id") id: Long): Category? {
        return categoryDao.getReferenceById(id)
    }
}