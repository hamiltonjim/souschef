/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

/**
 * Type of a unit. Unit IDs are unique within a given type, but not unique among all types.
 */
enum class UnitType {
    VOLUME, WEIGHT, NONE
}