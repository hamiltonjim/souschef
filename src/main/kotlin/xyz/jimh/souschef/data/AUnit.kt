/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import java.io.Serializable
import xyz.jimh.souschef.config.UnitType
import xyz.jimh.souschef.data.AUnit.Ident

/**
 * Class that can be either a weight or a volume. (Called [AUnit] because [Unit]
 * is something else entirely in Kotlin.)
 *
 * Corresponds to the "units" table in the database.
 * @property id unique ID per type
 * @property name name of the unit
 * @property type [UnitType]
 * @property inBase for weights, value in grams; for volumes, value in ml
 * @property intl if true, an international unit; if false, an English unit
 * @property abbrev the standard abbreviation for the unit (or null, if there is none)
 * @constructor Usually called automagically by retrieving a record from the database.
 */
@Schema(description = "A generic unit, either a weight or a volume")
@Entity(name = "units")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
@IdClass(Ident::class)
data class AUnit(
    @Schema(description = "Unique ID of the unit within its type", example = "1")
    @Id override var id: Long?,
    @Schema(description = "Name of the unit", example = "cup")
    override var name: String,
    @Id @Enumerated(EnumType.STRING) var type: UnitType,
    @Schema(description = "Size of the unit, in grams for weights, in milliliters for volumes", example = "42")
    override var inBase: Double,
    @Schema(description = "Whether the unit is from the International System", example = "false")
    override var intl: Boolean,
    @Schema(description = "Standard abbreviation", example = "c.")
    override var abbrev: String? = null,
) : UnitBase(name, inBase, intl, abbrev, id) {

    /**
     * Capsule for the full ID of a unit ([id] + [type])
     */
    @Embeddable
    data class Ident(val id: Long, val type: UnitType) : Serializable
}
