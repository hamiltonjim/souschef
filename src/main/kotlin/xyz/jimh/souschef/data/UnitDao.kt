/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Hidden
interface UnitDao : JpaRepository<AUnit, Long> {
    fun findByName(unitName: String): AUnit?
    fun findByAbbrev(abbrev: String): AUnit?

    @Query("SELECT A FROM units A WHERE (A.name = :name or A.abbrev = :name) and A.type = :type")
    fun findByAnyNameAndType(name: String, type: UnitType): AUnit?
    fun findAllByType(type: UnitType): List<AUnit>
    fun findAllByTypeAndIntlTrue(type: UnitType): List<AUnit>
    fun findAllByTypeAndIntlFalse(type: UnitType): List<AUnit>
}