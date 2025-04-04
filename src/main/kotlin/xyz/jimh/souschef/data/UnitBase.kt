/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

// All constructor args have default values, so that we get a free no-arg constructor
abstract class UnitBase(
    open var name: String = "",
    open var inBase: Double = 0.0,
    open var intl: Boolean = false,
    open var abbrev: String? = null,
    open var id: Long? = null
)