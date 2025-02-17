// Copyright © 2025 Jim Hamilton. All rights reserved.

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.Hidden
import java.util.*
import org.springframework.data.jpa.repository.JpaRepository

@Hidden
interface FoodItemDao : JpaRepository<FoodItem, Long> {
    fun findByName(name: String): Optional<FoodItem>
}