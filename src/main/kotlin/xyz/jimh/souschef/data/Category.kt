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

/**
 * Record in the categories table.
 * @property name category name String
 * @property id unique id Long
 */
@Entity(name = "categories")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
@Schema(description = "A recipe category")
data class Category(
    @field:Schema(description = "name of the category", type = "string")
    var name: String,
    @field:Schema(description = "ID of the category", type = "long", nullable = true, required = false)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null)