/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Abstract base class for units ([Weight] and [Volume], and also the catchall
 * [AUnit]). All constructor args have default values, so that we get a free
 * no-arg constructor.
 * @property name the unit's name.
 * @property inBase the value in grams for a [Weight], or milliliters for a [Volume]
 * @property intl true if an international unit, false if an English unit ("freedom unit").
 * @property abbrev the standard abbreviation for the unit, or null if there is none.
 * @property id the unique ID (within its type) of the unit.
 * @property altAbbrev a commonly used alternate abbreviation; as in T. for tablespoon (instead of tbsp.).
 */
abstract class UnitBase(
    open var name: String = "",
    @field:Schema(description = "Amount of the unit in its base unit", example = "1")
    open var inBase: Double = 0.0,
    @field:Schema(description = "Whether the unit is defined by the International System", example = "true")
    open var intl: Boolean = false,
    @field:Schema(description = "The unit's standard abbreviation", example = "lb.")
    open var abbrev: String? = null,
    @field:Schema(description = "The unit's ID, assigned by the database", example = "1")
    open var id: Long? = null,
    @field:Schema(description = "An alternative abbreviation, if in common use", example = "T.")
    open var altAbbrev: String? = null,
)