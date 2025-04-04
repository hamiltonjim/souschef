/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.utility

import kotlin.math.pow
import kotlin.math.round

/**
 * Rounds the receiver to the given number pf [decimals]. If [decimals] is
 * negative, rounds to the appropriate place: -1 will round to the nearest
 * 10, -3 to the nearest thousand, etc.
 */
fun Double.round(decimals: Int): Double {
    val multiplier = 10.0.pow(decimals)
    return round(this * multiplier) / multiplier
}
