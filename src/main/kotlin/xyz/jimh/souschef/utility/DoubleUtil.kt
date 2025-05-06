/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.utility

import kotlin.math.pow
import kotlin.math.round

/**
 * Rounds the receiver to the given number of [decimals]. If [decimals] is
 * negative, rounds to the appropriate place: -1 will round to the nearest
 * 10, -3 to the nearest thousand, etc.
 *
 * Examples:
 *  * PI.round(2) => 3.14<br>
 *  * 913.0.round(-1) => 910<br><br>
 *  * 299792458.0.round(-6) => 300000000
 */
fun Double.round(decimals: Int): Double {
    val multiplier = 10.0.pow(decimals)
    return round(this * multiplier) / multiplier
}
