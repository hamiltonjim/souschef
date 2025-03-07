/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.Hidden
import java.util.*
import org.springframework.data.jpa.repository.JpaRepository

@Hidden
interface CategoryDao : JpaRepository<Category, Long> {
    fun findAllByIdNotNullOrderByName(): List<Category>
    fun findByName(name: String): Optional<Category>
}