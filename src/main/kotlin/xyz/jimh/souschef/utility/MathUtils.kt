// Copyright © 2025 Jim Hamilton. All rights reserved.

package xyz.jimh.souschef.utility

import kotlin.math.abs

/**
 * Object that provides "close enough" comparisons for Double. For
 * the purpose of Recipe measurement, "close enough" is about 0.01.
 */
object MathUtils {

    /**
     *  for measuring ingredients, .01 of a unit is "close enough"
     */
    const val EPSILON = 1.0e-2

    /**
     * Test that [value] is greater than or equal to [test], within [EPSILON].
     */
    fun geEpsilon(value: Double, test: Double): Boolean {
        return value >= test - EPSILON
    }

    /**
     * Test that [value] is greater than [test], within [EPSILON].
     */
    fun gtEpsilon(value: Double, test: Double): Boolean {
        return value > test - EPSILON
    }

    /**
     * Test that [value] is less than [test], within [EPSILON].
     */
    fun ltEpsilon(value: Double, test: Double): Boolean {
        return value < test + EPSILON
    }

    /**
     * Test that [value] is less than or equal to [test], within [EPSILON].
     */
    fun leEpsilon(value: Double, test: Double): Boolean {
        return value <= test + EPSILON
    }

    /**
     * Test that [value] equal to [test], within [EPSILON].
     */
    fun eqEpsilon(value: Double, test: Double): Boolean {
        return abs(value - test) < EPSILON
    }

    /**
     * Test that [value] is not equal to [test], within [EPSILON].
     */
    fun neEpsilon(value: Double, test: Double): Boolean {
        return abs(value - test) >= EPSILON
    }
}