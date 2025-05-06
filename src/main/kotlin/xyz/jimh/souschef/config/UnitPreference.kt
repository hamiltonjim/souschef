/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

/**
 * Enumeration for which types of units are preferred. Names are self-explanatory.
 */
enum class UnitPreference(private val label: String) {
    /**
     * Preference for using international system units (milliliters, grams)
     */
    INTERNATIONAL("International"),

    /**
     * Preference for using English units, as is done in the USA and practically nowhere else.
     */
    ENGLISH("English"),

    /**
     * Preference for not choosing a preference in units. "If you choose not to decide,
     * you still have made a choice." -- Neil Peart
     */
    ANY("Any");

    /**
     * Gets the user-visible string for the preference.
     */
    fun getLabel(): String = label
}