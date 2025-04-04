/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * Interface to the weights database table.
 */
@Hidden
interface WeightDao : JpaRepository<Weight, Long> {
    /**
     * Find a [Weight] whose name or abbreviation matches [name]. Can return null.
     */
    @Query("select w from weights w where w.name = :name or w.abbrev = :name")
    fun findByAnyName(name: String): Weight?

    /**
     * Find all [Weight]s, by increasing mass, greater than [gramsGreaterThan] grams.
     * Kluge: always called with argument 0.0, because without any field given,
     * [JpaRepository] refused to synthesize a method to do "order by".
     */
    fun findAllByInBaseGreaterThanOrderByInBase(gramsGreaterThan: Double): List<Weight>

    /**
     * Find all English [Weight]s ordered by increasing mass.
     */
    fun findAllByIntlIsFalseOrderByInBase() : List<Weight>

    /**
     * Find all international [Weight]s ordered by increasing mass.
     */
    fun findAllByIntlIsTrueOrderByInBase() : List<Weight>
}