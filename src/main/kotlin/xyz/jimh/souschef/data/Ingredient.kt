// Copyright © 2025 Jim Hamilton. All rights reserved.

package xyz.jimh.souschef.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity(name = "ingredients")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
class Ingredient(
    var itemId: Long,
    var amount: Double,
    var unit: String?,
    var recipeId: Long,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        return this.itemId == (other as Ingredient).itemId && this.recipeId == other.recipeId
    }

    override fun hashCode(): Int {
        return itemId.hashCode() * 31 + recipeId.hashCode()
    }
}