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
 *  * PI.round(2) &rarr; 3.14<br>
 *  * 913.0.round(-1) &rarr; 910<br><br>
 *  * 299792458.0.round(-6) &rarr; 300000000
 */
fun Double.round(decimals: Int): Double {
    val multiplier = when (decimals) {
        0 -> 1.0
        1 -> 10.0
        2 -> 100.0
        3 -> 1000.0
        -1 -> 0.1
        -2 -> 0.01
        -3 -> 0.001
        else -> 10.0.pow(decimals)
    }
    return round(this * multiplier) / multiplier
}
