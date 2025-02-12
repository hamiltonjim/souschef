// Copyright © 2025 Jim Hamilton. All rights reserved.

package xyz.jimh.souschef.data

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryDao : JpaRepository<Category, Long> {
    fun findAllByIdNotNullOrderByName(): List<Category>
    fun findByName(name: String): Optional<Category>
}