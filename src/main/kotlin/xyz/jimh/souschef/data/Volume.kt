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
 * Class that represents a volume measurement.
 * @constructor Builds with properties.
 * @property name the unit name
 * @property inBase the unit's value in milliliters
 * @property abbrev the standard abbreviation for the unit, or null if there is none
 * @property intl true if an international unit, false if an English (or "freedom") unit
 * @property id the unique (within volumes) ID; assigned by the database
 */
@Schema(description = "A unit of volume")
@Entity(name = "volumes")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
data class Volume(
    @Schema(description = "Name of the unit", example = "cup")
    override var name: String,
    @Schema(description = "The amount of milliliters for 1 of this unit", example = "236.588")
    @Column(name = "in_ml") override var inBase: Double,
    @Schema(description = "Whether the unit is from the International System", example = "false")
    override var intl: Boolean,
    @Schema(description = "Standard abbreviation for the unit", example = "c.")
    override var abbrev: String? = null,
    @Schema(description = "ID assigned by the database", example = "12")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) override var id: Long? = null
) : UnitBase(name, inBase, intl, abbrev, id) {
    /**
     * Builds a [Volume] from an [AUnit]
     */
    constructor(unit: AUnit) : this(unit.name, unit.inBase, unit.intl, unit.abbrev, unit.id)
}

