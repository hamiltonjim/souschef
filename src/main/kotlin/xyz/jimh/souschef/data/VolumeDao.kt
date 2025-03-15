/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Hidden
interface VolumeDao : JpaRepository<Volume, Long> {
    @Query("select v from volumes v where v.name = :name or v.abbrev = :name")
    fun findByAnyName(name: String): Volume?
    fun findAllByInBaseGreaterThanOrderByInBase(mlGreaterThan: Double): List<Volume>
    fun findAllByIntlIsFalseOrderByInBase(): List<Volume>
    fun findAllByIntlIsTrueOrderByInBase(): List<Volume>
}