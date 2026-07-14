/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

// Kluge to "uninitialize" the lateinit field
fun resetLateInitField(target: Any, fieldName: String) {
    val clazz = if (target is Class<*>) target else target.javaClass
    val field = try {
        clazz.getDeclaredField(fieldName)
    } catch (e: NoSuchFieldException) {
        // If it's a companion object property, it might be in the outer class
        if (clazz.name.endsWith("$" + "Companion")) {
            val outerClassName = clazz.name.substringBefore("$" + "Companion")
            Class.forName(outerClassName).getDeclaredField(fieldName)
        } else {
            throw e
        }
    }
    field.isAccessible = true
    field.set(if (java.lang.reflect.Modifier.isStatic(field.modifiers)) null else target, null)
}

