/*
 * Copyright © 2025 Jim Hamilton.
 * All rights reserved.
 */

package xyz.jimh.souschef.config

enum class UnitAbbrev(private val label: String) {
    FULL_NAME("Full Name"),
    ABBREVIATION("Abbreviation");

    fun getLabel(): String = label
}