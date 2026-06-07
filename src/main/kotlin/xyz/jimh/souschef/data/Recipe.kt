/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Objects
import kotlin.math.min
import kotlinx.datetime.format

/**
 * A recipe; a record in the "recipes" table.
 *
 * @property name
 * @property directions How to stir, etc.
 * @property servings How many servings does the recipe provide
 * @property categoryId ID of the recipe's [Category]
 * @property id Unique ID
 * @property deleted true if the recipe is marked deleted
 * @property deletedOn date/time that the recipe was deleted, or null if not deleted
 */
@Schema(description = "Description of the recipe")
@Entity(name = "recipes")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
data class Recipe(
    @field:Schema(description = "Name of the recipe", example = "Apple Pie")
    var name: String,
    @field:Schema(description = "Recipe directions", example = "Mix, stir, etc.", required = false)
    var directions: String,
    @field:Schema(description = "How many servings in the original", example = "3", required = true)
    var servings: Int,
    @field:Schema(description = "Category for the recipe", example = "1", required = true)
    var categoryId: Long,
    @field:Schema(description = "Recipe ID, initially assigned by the database", example = "12", required = false)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @field:Schema(description = "Whether the recipe is deleted", required = false)
    var deleted: Boolean = false,
    @field:Schema(description = "Date/time the recipe was deleted, or null", example = "2025-03-29T12:34:56", required = false)
    var deletedOn: Instant? = null
) {
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Recipe) return false
        return name == other.name && directions == other.directions && servings == other.servings &&
                categoryId == other.categoryId && deleted == other.deleted
    }

    override fun hashCode(): Int {
        return Objects.hash(name, directions, servings, categoryId, deleted)
    }

    override fun toString(): String {
        val showDir = directions.substring(0, min(10, directions.length))
        val showDel = if (deleted) {
            "Deleted on ${deletedOn?.atZone(ZoneId.systemDefault())?.format(DateTimeFormatter.ISO_DATE_TIME)}"
        } else {
            "Active"
        }
        return "Recipe(name='$name', directions='$showDir', servings=$servings, categoryId=$categoryId, id=$id, $showDel)"
    }
}
