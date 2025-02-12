package xyz.jimh.souschef.config

// Copyright © 2025 Jim Hamilton. All rights reserved.

import kotlin.math.pow
import kotlin.math.round

fun Double.round(decimals: Int): Double {
    val multiplier = 10.0.pow(decimals)
    return round(this * multiplier) / multiplier
}
