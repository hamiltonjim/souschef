/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

// All constructor args have default values, so that we get a free no-arg constructor
open class UnitBase(
    open var name: String = "",
    open var inBase: Double = 0.0,
    open var intl: Boolean = false,
    open var abbrev: String? = null,
    open var id: Long? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnitBase) return false

        if (name != other.name) return false
        if (inBase != other.inBase) return false
        if (intl != other.intl) return false
        if (abbrev != other.abbrev) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + inBase.hashCode()
        result = 31 * result + intl.hashCode()
        result = 31 * result + (abbrev?.hashCode() ?: 0)
        result = 31 * result + (id?.hashCode() ?: 0)
        return result
    }
}