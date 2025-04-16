/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import xyz.jimh.souschef.config.UnitType

/**
 * Interface to the "units" view (combination of "volumes" and "weights").
 */
@Hidden     // from Swagger
interface UnitDao : JpaRepository<AUnit, Long> {
    /**
     * Find a unit by its name. Can return null.
     */
    fun findByName(unitName: String): AUnit?

    /**
     * Find a unit by its standard abbreviation. Can return null
     */
    fun findByAbbrev(abbrev: String): AUnit?

    /**
     * Find a unit by its type and matching either name or
     * abbreviation. Can return null.
     */
    @Query("SELECT A FROM units A WHERE (A.name = :name or A.abbrev = :name) and A.type = :type")
    fun findByAnyNameAndType(name: String, type: UnitType): AUnit?

    /**
     * Finds all units of the given type.
     */
    fun findAllByType(type: UnitType): List<AUnit>

    /**
     * Finds all international units of the given type.
     */
    fun findAllByTypeAndIntlTrue(type: UnitType): List<AUnit>

    /**
     * Finds all English units of the given type.
     */
    fun findAllByTypeAndIntlFalse(type: UnitType): List<AUnit>
}