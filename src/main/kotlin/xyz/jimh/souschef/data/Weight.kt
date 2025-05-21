/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema
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
 * @property altAbbrev any alternative abbreviations in common usage.
 */
@Schema(description = "A unit of weight")
@Entity(name = "weights")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
data class Weight(
    @Schema(description = "Name of the unit", example = "pound")
    override var name: String,
    @Schema(description = "Number of grams for this unit", example = "453.59")
    @Column(name = "in_grams") override var inBase: Double,
    @Schema(description = "Whether the unit is from the International System", example = "false")
    override var intl: Boolean,
    @Schema(description = "Standard abbreviation for the unit", example = "lb.")
    override var abbrev: String? = null,
    @Schema(description = "ID assigned by the database", example = "1")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) override var id: Long? = null,
    @Schema(description = "Any alternative abbreviations in common use", example = "T.")
    @Column(name = "alt_abbrev") override var altAbbrev: String? = null,
) : UnitBase(name, inBase, intl, abbrev, id, altAbbrev) {
    /**
     * Builds a [Weight] from an [AUnit]
     */
    constructor(unit: AUnit) : this(unit.name, unit.inBase, unit.intl, unit.abbrev, unit.id, unit.altAbbrev)
}
