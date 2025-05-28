/*
 * Copyright (c) 2025 Jim Hamilton. All rights reserved.
 */

package xyz.jimh.souschef.utility


/**
 * Extension to [List], specifically containing [String]: Returns true if the receiver list contains any
 * string in the argument [list]. To match case, pass [ignoreCase] = false (defaults to true).
 */
fun List<String>.containsAny(list: List<String>, ignoreCase: Boolean = true): Boolean {
    for (item in this) {
        for (other in list) {
            if (item.equals(other, ignoreCase))
                return true
        }
    }

    return false
}