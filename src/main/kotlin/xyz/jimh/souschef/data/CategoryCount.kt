/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.Objects

/**
 * A record returned by [CountDao.getCategoryCounts]
 * @property category name of a category
 * @property count number of recipes in that category
 */
@Entity
@Schema(description = "Count of recipes for a category")
data class CategoryCount(
    @field:Schema(description = "Category name", example = "Desserts")
    @Id val category: String,
    @field:Schema(description = "Number of recipes in the category", example = "5")
    @Column(name = "cnt") val count: Int
) {
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is CategoryCount) return false
        return category == other.category
    }

    override fun hashCode(): Int {
        return Objects.hash(category, count)
    }

    override fun toString(): String {
        return "CategoryCount(category='$category', count=$count)"
    }
}
