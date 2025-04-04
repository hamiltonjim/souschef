/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

/**
 * A record returned by [CountDao.getCategoryCounts]
 * @property category name of a category
 * @property count number of recipes in that category
 */
@Entity
@Schema(description = "Count of recipes for a category")
data class CategoryCount(@Id val category: String, @Column(name="cnt") val count: Int)
