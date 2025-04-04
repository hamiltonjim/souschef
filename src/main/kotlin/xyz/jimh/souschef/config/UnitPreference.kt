/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

/**
 * Enumeration for which types of units are preferred. Names are self-explanatory.
 */
enum class UnitPreference(private val label: String) {
    INTERNATIONAL("International"),
    ENGLISH("English"),
    ANY("Any");

    /**
     * Gets the user-visible string for the preference.
     */
    fun getLabel(): String = label
}