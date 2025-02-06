package xyz.jimh.souschef.data

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository

interface RecipeDao : JpaRepository<Recipe, Long> {
    fun findByName(name: String): Optional<Recipe>
    fun findAllByDeletedIsFalse(): List<Recipe>
    fun findAllByCategoryIdAndDeletedIsFalse(category: Long): List<Recipe>
}