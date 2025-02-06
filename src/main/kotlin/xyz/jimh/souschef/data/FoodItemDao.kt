package xyz.jimh.souschef.data

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository

interface FoodItemDao : JpaRepository<FoodItem, Long> {
    fun findByName(name: String): Optional<FoodItem>
}