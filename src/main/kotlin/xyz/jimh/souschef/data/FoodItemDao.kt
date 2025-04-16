/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.Hidden
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Interface to the database "foods" table
 */
@Hidden     // from Swagger
interface FoodItemDao : JpaRepository<FoodItem, Long> {
    /**
     * Get a food item by its name. May return [Optional.empty]
     */
    fun findByName(name: String): Optional<FoodItem>
}