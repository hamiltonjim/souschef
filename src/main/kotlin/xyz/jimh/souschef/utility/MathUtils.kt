// Copyright © 2025 Jim Hamilton. All rights reserved.

package xyz.jimh.souschef.utility

import kotlin.math.abs

object MathUtils {

    // for measuring ingredients, .01 of a unit is "close enough"
    const val EPSILON = 1.0e-2

    fun geEpsilon(value: Double, test: Double): Boolean {
        return value >= test - EPSILON
    }

    fun gtEpsilon(value: Double, test: Double): Boolean {
        return value > test - EPSILON
    }

    fun ltEpsilon(value: Double, test: Double): Boolean {
        return value < test + EPSILON
    }

    fun leEpsilon(value: Double, test: Double): Boolean {
        return value <= test + EPSILON
    }

    fun eqEpsilon(value: Double, test: Double): Boolean {
        return abs(value - test) < EPSILON
    }

    fun neEpsilon(value: Double, test: Double): Boolean {
        return abs(value - test) >= EPSILON
    }
}