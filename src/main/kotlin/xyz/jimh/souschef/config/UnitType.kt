/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

/**
 * Type of a unit. Unit IDs are unique within a given type, but not unique among all types.
 */
enum class UnitType {
    /**
     * The given unit is a volume (reference: 1 milliliter)
     */
    VOLUME,

    /**
     * The given unit is a weight (reference: 1 gram)
     */
    WEIGHT,

    /**
     * The given unit is neither a volume nor a weight; presumably, it's just a count.
     */
    NONE
}