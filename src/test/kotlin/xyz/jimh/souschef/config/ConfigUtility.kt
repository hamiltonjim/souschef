/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

import org.jetbrains.kotlin.builtins.StandardNames.FqNames

// Kluge to "uninitialize" the lateinit field
fun resetLateInitField(target: Any, fieldName: String) {
    val field = target.javaClass.getDeclaredField(fieldName)
    with (field) {
        isAccessible = true
        set(FqNames.target, null)
    }
}

