/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.Hidden
import java.util.*
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Interface to the categories table in the database.
 */
@Hidden
interface CategoryDao : JpaRepository<Category, Long> {
    /**
     * Returns all categories (alphabetized) as long as they are
     * properly stored.
     */
    fun findAllByIdNotNullOrderByName(): List<Category>

    /**
     * Returns all categories (alphabetized).
     */
    fun findByName(name: String): Optional<Category>
}