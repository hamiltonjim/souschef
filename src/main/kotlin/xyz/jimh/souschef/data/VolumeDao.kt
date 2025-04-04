/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * Interface to the volumes table in the database.
 */
@Hidden
interface VolumeDao : JpaRepository<Volume, Long> {
    /**
     * Find a [Volume] where the given name matches either the name or
     * the abbreviation. Can return null.
     */
    @Query("select v from volumes v where v.name = :name or v.abbrev = :name")
    fun findByAnyName(name: String): Volume?

    /**
     * Find all [Volume]s in increasing size, where size is at least
     * [mlGreaterThan] milliliters. Kluge: always called with argument 0.0,
     * because without any field given, [JpaRepository] refused to synthesize
     * a method to do "order by".
     */
    fun findAllByInBaseGreaterThanOrderByInBase(mlGreaterThan: Double): List<Volume>

    /**
     * Find all English [Volume]s, ordered by increasing size.
     */
    fun findAllByIntlIsFalseOrderByInBase(): List<Volume>

    /**
     * Find all international [Volume]s, ordered by increasing size.
     */
    fun findAllByIntlIsTrueOrderByInBase(): List<Volume>
}