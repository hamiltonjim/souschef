/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

/**
 * Enumeration of ways to display units.
 */
enum class UnitAbbrev(private val label: String) {
    FULL_NAME("Full Name"),
    ABBREVIATION("Abbreviation");

    /**
     * Gets the user-visible string for whether preferring abbreviations or not.
     */
    fun getLabel(): String = label
}