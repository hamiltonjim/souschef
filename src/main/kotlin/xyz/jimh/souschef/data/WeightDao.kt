/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Hidden
interface WeightDao : JpaRepository<Weight, Long> {
    @Query("select w from weights w where w.name = :name or w.abbrev = :name")
    fun findByAnyName(name: String): Weight?
    fun findAllByInGramsGreaterThanOrderByInGrams(gramsGreaterThan: Double): List<Weight>
    fun findAllByIntlIsFalseOrderByInGrams() : List<Weight>
    fun findAllByIntlIsTrueOrderByInGrams() : List<Weight>
}