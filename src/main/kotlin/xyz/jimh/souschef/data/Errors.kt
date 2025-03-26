/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.data

import kotlinx.serialization.Serializable

@Serializable
data class Errors(val errors: List<String>) {
    fun isEmpty() = errors.isEmpty()
    fun isNotEmpty() = errors.isNotEmpty()
    fun size() = errors.size
}
