/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import kotlinx.serialization.Serializable

/**
 * A class that accumulates error messages, so that they can all be displayed at once.
 * @property errors list of String
 */
@Serializable
data class Errors(val errors: List<String>) {
    /**
     * True if the list is empty.
     */
    fun isEmpty() = errors.isEmpty()

    /**
     * True if the list has at least one item.
     */
    fun isNotEmpty() = errors.isNotEmpty()

    /**
     * Number of errors in the list.
     */
    fun size() = errors.size
}
