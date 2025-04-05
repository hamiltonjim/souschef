/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

//
/**
 * Abstract base class for units ([Weight] and [Volume], and also the catchall
 * [AUnit]. All constructor args have default values, so that we get a free
 * no-arg constructor.
 * @property name
 * @property inBase the value in grams for a [Weight], or milliliters for a [Volume]
 * @property intl true if an international unit, false if an English unit ("freedom unit").
 * @property abbrev the standard abbreviation for the unit, or null if there is none.
 * @property id the unique ID (within its type) of the unit.
 */
abstract class UnitBase(
    open var name: String = "",
    open var inBase: Double = 0.0,
    open var intl: Boolean = false,
    open var abbrev: String? = null,
    open var id: Long? = null
)