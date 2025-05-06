/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

/**
 * Enumeration of ways to display units.
 */
enum class UnitAbbrev(private val label: String) {
    /**
     * Preference for using the full names of units.
     */
    FULL_NAME("Full Name"),

    /**
     * Preference for using the units' abbreviations.
     */
    ABBREVIATION("Abbreviation");

    /**
     * Gets the user-visible string for whether preferring abbreviations or not.
     */
    fun getLabel(): String = label
}