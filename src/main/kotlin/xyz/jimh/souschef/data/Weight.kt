/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

/**
 * Class that represents a weight measurement.
 * @constructor Builds with properties.
 * @property name the unit name
 * @property inBase the unit's value in grams
 * @property abbrev the standard abbreviation for the unit, or null if there is none
 * @property intl true if an international unit, false if an English (or "freedom") unit
 * @property id the unique (within volumes) ID; assigned by the database
 */
@Entity(name = "weights")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
data class Weight(
    override var name: String,
    @Column(name = "in_grams") override var inBase: Double,
    override var intl: Boolean,
    override var abbrev: String? = null,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) override var id: Long? = null
) : UnitBase(name, inBase, intl, abbrev, id) {
    /**
     * Builds a [Weight] from an [AUnit]
     */
    constructor(unit: AUnit) : this(unit.name, unit.inBase, unit.intl, unit.abbrev, unit.id)


}
